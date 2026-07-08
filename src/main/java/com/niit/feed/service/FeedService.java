package com.niit.feed.service;

import com.niit.feed.config.RoutingDataSource;
import com.niit.feed.mapper.BlockRelationMapper;
import com.niit.feed.mapper.FeedInboxMapper;
import com.niit.feed.mapper.FeedOutboxMapper;
import com.niit.feed.mapper.FeedPostMapper;
import com.niit.feed.mapper.FollowRelationMapper;
import com.niit.feed.mapper.UserMapper;
import com.niit.feed.model.FeedInbox;
import com.niit.feed.model.FeedOutbox;
import com.niit.feed.model.FeedPost;
import com.niit.feed.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);

    private final FeedPostMapper postMapper;
    private final FeedInboxMapper inboxMapper;
    private final FeedOutboxMapper outboxMapper;
    private final FollowRelationMapper followMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final BlockRelationMapper blockMapper;

    public FeedService(FeedPostMapper postMapper,
                       FeedInboxMapper inboxMapper,
                       FeedOutboxMapper outboxMapper,
                       FollowRelationMapper followMapper,
                       UserMapper userMapper,
                       UserService userService,
                       BlockRelationMapper blockMapper) {
        this.postMapper = postMapper;
        this.inboxMapper = inboxMapper;
        this.outboxMapper = outboxMapper;
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.blockMapper = blockMapper;
    }

    /**
     * 推模式发帖：
     * 1. 写入 feed_post
     * 2. 查粉丝列表
     * 3. 批量写入每个粉丝的 feed_inbox
     */
    @Transactional
    public FeedPost createPost(Long userId, String content) {
        // TODO 1：发帖 → 强制走 Master + 记录发帖时间（5秒内读也走Master）
        RoutingDataSource.setMaster();
        RoutingDataSource.recordPostTime(userId);

        // 第 1 步：写入 feed_post（三天可见：visible_until = NOW() + 72h）
        FeedPost post = new FeedPost();
        post.setUserId(userId);
        post.setContent(content);
        post.setVisibleUntil(LocalDateTime.now().plusHours(72));
        postMapper.insert(post);
        post.setCreatedAt(LocalDateTime.now());  // @Options 只回填 id，需要手动设 createdAt
        log.info("发帖成功: postId={}, userId={}, visibleUntil={}", post.getId(), userId, post.getVisibleUntil());

        // 更新活跃时间
        userMapper.updateLastActiveAt(userId);

        // 第 2 步：判断是不是大V
        User user = userMapper.findById(userId);
        boolean isBigV = user != null && user.getIsBigV();

        if (isBigV) {
            // 大V：只写 1 条到 outbox（拉模式）
            FeedOutbox outbox = new FeedOutbox();
            outbox.setUserId(userId);
            outbox.setPostId(post.getId());
            outbox.setContent(content);
            outboxMapper.insert(outbox);
            post.setSource("pull");
            log.info("大V发帖(拉模式): postId={}, userId={}, 只写outbox", post.getId(), userId);
        } else {
            // 普通用户：推模式 —— 只推给 24h 内活跃的粉丝
            List<Long> activeFollowerIds = userMapper.findActiveFollowerIds(userId);
            if (activeFollowerIds == null) {
                activeFollowerIds = new ArrayList<>();
            }
            if (!activeFollowerIds.contains(userId)) {
                activeFollowerIds.add(userId);
            }
            log.info("分层推送: userId={}, 活跃粉丝数={}", userId, activeFollowerIds.size());

            List<FeedInbox> inboxList = new ArrayList<>();
            for (Long followerId : activeFollowerIds) {
                FeedInbox inbox = new FeedInbox();
                inbox.setUserId(followerId);
                inbox.setPostId(post.getId());
                inbox.setFromUserId(userId);
                inboxList.add(inbox);
            }
            inboxMapper.batchInsert(inboxList);
            post.setSource("push");
            log.info("收件箱写入完成: 共写入 {} 条", inboxList.size());
        }

        post.setUserName(user != null ? user.getName() : "未知");

        return post;
    }

    /**
     * 读收件箱（推模式）
     */
    public Map<String, Object> getInbox(Long userId, Long cursor, int size) {
        // 第 1 步：多查 1 条，用于判断是否还有下一页
        List<FeedInbox> list = inboxMapper.findByUserId(userId, cursor, size + 1);

        // 第 2 步：判断 hasMore
        boolean hasMore = list.size() > size;

        // 第 3 步：如果还有更多，截掉多查的那条
        if (hasMore) {
            list = list.subList(0, size);
        }

        // 第 4 步：计算下一页的游标
        Long nextCursor = hasMore ? list.get(list.size() - 1).getId() : null;

        // 第 5 步：返回结果（用 HashMap，因为 nextCursor 可能为 null）
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("hasMore", hasMore);
        result.put("nextCursor", nextCursor);
        return result;
    }

    /**
     * 读发件箱（拉模式）
     */
    public Map<String, Object> getOutbox(Long userId, Long cursor, int size) {
        List<FeedOutbox> list = outboxMapper.findByUserId(userId, cursor, size + 1);
        boolean hasMore = list.size() > size;
        if (hasMore) {
            list = list.subList(0, size);
        }
        Long nextCursor = hasMore ? list.get(list.size() - 1).getId() : null;

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("hasMore", hasMore);
        result.put("nextCursor", nextCursor);
        return result;
    }

    /**
     * Timeline：推拉合并 — inbox(推) + outbox(拉) → 合并排序
     */
    public Map<String, Object> getTimeline(Long userId, Long cursor, int size) {
        // TODO 2：读操作 → 优先走 Slave，但发帖后 5 秒内走 Master（防止主从延迟）
        if (RoutingDataSource.shouldReadMaster(userId)) {
            RoutingDataSource.setMaster();
        } else {
            RoutingDataSource.setSlave();
        }

        List<Map<String, Object>> merged = new ArrayList<>();

        // 1. 收件箱（推模式 — 普通用户的帖子）
        List<FeedInbox> inboxList = inboxMapper.findByUserId(userId, null, size + 1);
        if (inboxList != null) {
            for (FeedInbox item : inboxList) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", item.getId());
                entry.put("postId", item.getPostId());
                entry.put("userId", item.getFromUserId());
                entry.put("content", item.getContent());
                entry.put("createdAt", item.getCreatedAt());
                entry.put("userName", item.getFromUserName());
                entry.put("source", "push");
                entry.put("isPinned", item.getIsPinned());
                merged.add(entry);
            }
        }

        // 2. 发件箱（拉模式 — 我关注的大V + 用户自己如果也是大V）
        List<Long> bigVIds = followMapper.findFollowedBigVIds(userId);
        if (bigVIds == null) bigVIds = new ArrayList<>();
        User currentUser = userMapper.findById(userId);
        if (currentUser != null && currentUser.getIsBigV() && !bigVIds.contains(userId)) {
            bigVIds.add(userId);  // 大V自己发的帖子自己也要能看到
        }
        if (!bigVIds.isEmpty()) {
            List<FeedOutbox> outboxList = outboxMapper.findByUserIds(bigVIds, null, size + 1);
            if (outboxList != null) {
                for (FeedOutbox item : outboxList) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", item.getId());
                    entry.put("postId", item.getPostId());
                    entry.put("userId", item.getUserId());
                    entry.put("content", item.getContent());
                    entry.put("createdAt", item.getCreatedAt());
                    entry.put("userName", item.getUserName());
                    entry.put("source", "pull");
                    merged.add(entry);
                }
            }
        }

        // 3. 补拉（catch-up）：如果我离线 > 24h，拉取遗漏的普通用户帖子
        if (currentUser != null && currentUser.getLastActiveAt() != null) {
            Duration inactive = Duration.between(currentUser.getLastActiveAt(), LocalDateTime.now());
            if (inactive.toHours() >= 24) {
                List<Long> nonBigVIds = followMapper.findFollowedNonBigVIds(userId);
                if (nonBigVIds != null && !nonBigVIds.isEmpty()) {
                    List<FeedPost> missedPosts = postMapper.findMissedPosts(
                            userId, nonBigVIds, currentUser.getLastActiveAt(), size);
                    if (missedPosts != null) {
                        for (FeedPost p : missedPosts) {
                            Map<String, Object> entry = new LinkedHashMap<>();
                            entry.put("id", p.getId());
                            entry.put("postId", p.getId());
                            entry.put("userId", p.getUserId());
                            entry.put("content", p.getContent());
                            entry.put("createdAt", p.getCreatedAt());
                            entry.put("userName", p.getUserName());
                            entry.put("source", "catchup");
                            merged.add(entry);
                        }
                    }
                }
            }
        }

        // 4. 过滤被拉黑用户的帖子
        List<Long> blockedIds = blockMapper.findBlockedUserIds(userId);
        if (blockedIds != null && !blockedIds.isEmpty()) {
            Set<Long> blockedSet = new HashSet<>(blockedIds);
            merged.removeIf(item -> {
                Object uid = item.get("userId");
                return uid instanceof Long && blockedSet.contains(uid);
            });
        }

        // 5. 按 isPinned DESC, createdAt DESC 排序
        merged.sort((a, b) -> {
            boolean pinnedA = Boolean.TRUE.equals(a.get("isPinned"));
            boolean pinnedB = Boolean.TRUE.equals(b.get("isPinned"));
            if (pinnedA != pinnedB) return pinnedA ? -1 : 1;
            return ((Comparable) b.get("createdAt")).compareTo(a.get("createdAt"));
        });

        // 6. 游标分页
        boolean hasMore = merged.size() > size;
        if (hasMore) {
            merged = merged.subList(0, size);
        }
        Long nextCursor = hasMore && !merged.isEmpty()
                ? (Long) merged.get(merged.size() - 1).get("id") : null;

        // 7. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", merged);
        result.put("hasMore", hasMore);
        result.put("nextCursor", nextCursor);
        return result;
    }

    /**
     * 大V阈值：粉丝数>=1000 则为大V
     */
    public int getBigVThreshold() {
        return 1000;
    }

    /**
     * 晋升大V：is_big_v=1 + 把历史帖子迁移到 outbox
     */
    @Transactional
    public void promoteToBigV(Long userId) {
        // TODO 4：晋升大V → 写操作（UPDATE + INSERT）→ 强制走 Master
        RoutingDataSource.setMaster();

        User user = userMapper.findById(userId);
        if (user == null || user.getIsBigV()) return;
        userMapper.updateBigV(userId);
        log.info("晋升大V: userId={}, 粉丝数={}", userId, user.getFollowerCount());
        // 把历史帖子迁移到 outbox
        List<FeedPost> posts = postMapper.findByUserId(userId);
        for (FeedPost p : posts) {
            FeedOutbox outbox = new FeedOutbox();
            outbox.setUserId(userId);
            outbox.setPostId(p.getId());
            outbox.setContent(p.getContent());
            outboxMapper.insert(outbox);
        }
        log.info("大V迁移完成: userId={}, 共{}条帖子写入outbox", userId, posts.size());
    }

    /**
     * 置顶/取消置顶
     */
    public void pinPost(Long userId, Long postId, boolean pinned) {
        if (pinned) {
            inboxMapper.pinPost(userId, postId);
        } else {
            inboxMapper.unpinPost(userId, postId);
        }
        log.info("置顶操作: userId={}, postId={}, pinned={}", userId, postId, pinned);
    }

    /**
     * 定时清理过期帖子（每1小时执行一次）
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredPosts() {
        RoutingDataSource.setMaster();
        int deleted = inboxMapper.deleteExpiredInbox();
        if (deleted > 0) {
            log.info("清理过期帖子: 删除{}条inbox", deleted);
        }
    }

    /**
     * 写扩散统计（调试面板用）
     */
    public Map<String, Object> getFanoutStats(Long userId) {
        // TODO 3：只读 → 走 Slave
        RoutingDataSource.setSlave();

        User user = userMapper.findById(userId);
        boolean isBigV = user != null && user.getIsBigV();
        int followerCount = user != null ? user.getFollowerCount() : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("lastPostFanoutCount", isBigV ? 1 : followerCount);
        stats.put("mode", isBigV ? "pull" : "push");
        stats.put("isBigV", isBigV);
        return stats;
    }
}
