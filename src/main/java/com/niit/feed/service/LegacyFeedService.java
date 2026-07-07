package com.niit.feed.service;

import com.niit.feed.mapper.FeedInboxMapper;
import com.niit.feed.mapper.FeedPostMapper;
import com.niit.feed.mapper.FollowRelationMapper;
import com.niit.feed.mapper.UserMapper;
import com.niit.feed.model.FeedInbox;
import com.niit.feed.model.FeedPost;
import com.niit.feed.model.User;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 遗留代码 — 包含 5 处缺陷
 *
 * 缺陷清单：
 * 1. N+1 查询：逐条 INSERT 替代批量写入
 * 2. 事务边界不清：feed_post 和 inbox 写入不在同一事务
 * 3. 敏感信息泄露：返回包含不必要字段
 * 4. 缺少索引：查询未利用索引
 * 5. 缺少日志：关键操作无日志输出
 */
@Service
public class LegacyFeedService {

    private final FeedPostMapper postMapper;
    private final FeedInboxMapper inboxMapper;
    private final FollowRelationMapper followMapper;
    private final UserMapper userMapper;

    public LegacyFeedService(FeedPostMapper postMapper,
                              FeedInboxMapper inboxMapper,
                              FollowRelationMapper followMapper,
                              UserMapper userMapper) {
        this.postMapper = postMapper;
        this.inboxMapper = inboxMapper;
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    // ==========================================
    // 缺陷 2：缺少 @Transactional 注解
    // 如果 inbox 写入中途失败，feed_post 已写入，数据不一致
    // ==========================================
    public FeedPost createPost(Long userId, String content) {
        // 缺陷 5：没有日志记录
        FeedPost post = new FeedPost();
        post.setUserId(userId);
        post.setContent(content);
        postMapper.insert(post);

        User user = userMapper.findById(userId);
        boolean isBigV = user != null && user.getIsBigV();

        if (!isBigV) {
            List<Long> followerIds = followMapper.findFollowerIds(userId);

            // ==========================================
            // 缺陷 1：N+1 查询 — 逐条 INSERT
            // 1000 粉丝 = 1000 次数据库往返
            // 应改为 batchInsert
            // ==========================================
            for (Long followerId : followerIds) {
                FeedInbox inbox = new FeedInbox();
                inbox.setUserId(followerId);
                inbox.setPostId(post.getId());
                inbox.setFromUserId(userId);
                inboxMapper.insert(inbox);
            }
        }

        // ==========================================
        // 缺陷 3：返回整个 User 对象，可能包含敏感字段
        // 应只返回必要的字段
        // ==========================================
        return post;
    }

    public Map<String, Object> getUserProfile(Long userId) {
        // 缺陷 3：直接返回 User 对象，可能包含敏感信息
        User user = userMapper.findById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("avatar", user.getAvatar());
        result.put("isBigV", user.getIsBigV());
        result.put("followerCount", user.getFollowerCount());
        result.put("lastActiveAt", user.getLastActiveAt());
        result.put("createdAt", user.getCreatedAt());
        result.put("updatedAt", user.getUpdatedAt());
        // 缺陷 3：如果后续 User 表新增 password 等字段，这里也会返回
        // 应该只返回前端需要的字段
        return result;
    }

    // 缺陷 5：整个类没有任何日志输出
    // 线上出现问题时无法排查

    // 缺陷 4：SQL 查询缺少索引（需检查 Mapper XML 和 EXPLAIN）
}
