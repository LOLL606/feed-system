package com.niit.feed.controller;

import com.niit.feed.config.RoutingDataSource;
import com.niit.feed.mapper.FeedInboxMapper;
import com.niit.feed.model.FeedInbox;
import com.niit.feed.service.FeedService;
import com.niit.feed.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final FeedService feedService;
    private final UserService userService;
    private final FeedInboxMapper inboxMapper;

    public DebugController(FeedService feedService, UserService userService,
                           FeedInboxMapper inboxMapper) {
        this.feedService = feedService;
        this.userService = userService;
        this.inboxMapper = inboxMapper;
    }

    @GetMapping("/fanout-stats")
    public Map<String, Object> fanoutStats() {
        Long userId = userService.getCurrentUserId();
        return feedService.getFanoutStats(userId);
    }

    @GetMapping("/feed-source")
    public Map<String, Object> feedSource() {
        Long userId = userService.getCurrentUserId();
        Map<String, Object> timeline = feedService.getTimeline(userId, null, 50);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) timeline.get("list");

        long pushCount = list.stream().filter(i -> "push".equals(i.get("source"))).count();
        long pullCount = list.stream().filter(i -> "pull".equals(i.get("source"))).count();
        long catchupCount = list.stream().filter(i -> "catchup".equals(i.get("source"))).count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("pushCount", pushCount);
        result.put("pullCount", pullCount);
        result.put("catchupCount", catchupCount);
        result.put("pushRatio", list.isEmpty() ? 0 : (double) pushCount / list.size());
        result.put("pullRatio", list.isEmpty() ? 0 : (double) pullCount / list.size());
        return result;
    }

    @GetMapping("/db-route")
    public Map<String, Object> dbRoute() {
        Long userId = userService.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("currentSource", RoutingDataSource.getCurrent());
        result.put("forceMasterActive", RoutingDataSource.shouldReadMaster(userId));
        result.put("masterWindowMs", 5000);
        return result;
    }

    @GetMapping("/promotion-check")
    public Map<String, Object> promotionCheck(@RequestParam Long userId) {
        return feedService.getFanoutStats(userId);
    }

    /**
     * 性能对比测试：逐条 INSERT vs 批量 INSERT
     */
    @GetMapping("/perf-test")
    public Map<String, Object> perfTest() {
        // 1. 逐条 INSERT（模拟 N+1）
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            FeedInbox inbox = new FeedInbox();
            inbox.setUserId(999L);
            inbox.setPostId((long) i);
            inbox.setFromUserId(1L);
            inboxMapper.insert(inbox);
        }
        long time1 = System.currentTimeMillis() - start1;

        // 清理测试数据
        // DELETE FROM feed_inbox WHERE user_id = 999

        // 2. 批量 INSERT（batchInsert）
        long start2 = System.currentTimeMillis();
        List<FeedInbox> batch = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            FeedInbox inbox = new FeedInbox();
            inbox.setUserId(998L);
            inbox.setPostId((long) i);
            inbox.setFromUserId(1L);
            batch.add(inbox);
        }
        inboxMapper.batchInsert(batch);
        long time2 = System.currentTimeMillis() - start2;

        return Map.of(
                "singleInsert", time1 + "ms",
                "batchInsert", time2 + "ms",
                "speedup", (time1 / Math.max(time2, 1)) + "x"
        );
    }
}
