package com.niit.feed.controller;

import com.niit.feed.model.FeedPost;
import com.niit.feed.service.FeedService;
import com.niit.feed.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    public FeedController(FeedService feedService, UserService userService) {
        this.feedService = feedService;
        this.userService = userService;
    }

    @PostMapping("/post")
    public FeedPost createPost(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        Long userId = userService.getCurrentUserId();
        return feedService.createPost(userId, content);
    }

    @GetMapping("/inbox")
    public Map<String, Object> getInbox(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userService.getCurrentUserId();
        return feedService.getInbox(userId, cursor, size);
    }

    @GetMapping("/outbox")
    public Map<String, Object> getOutbox(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userService.getCurrentUserId();
        return feedService.getOutbox(userId, cursor, size);
    }

    @PostMapping("/pin")
    public Map<String, Object> pinPost(@RequestBody Map<String, Object> body) {
        Long userId = userService.getCurrentUserId();
        Long postId = Long.valueOf(body.get("postId").toString());
        boolean pinned = Boolean.parseBoolean(body.getOrDefault("pinned", "true").toString());
        feedService.pinPost(userId, postId, pinned);
        return Map.of("ok", true);
    }

    @GetMapping("/timeline")
    public Map<String, Object> getTimeline(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userService.getCurrentUserId();
        return feedService.getTimeline(userId, cursor, size);
    }
}
