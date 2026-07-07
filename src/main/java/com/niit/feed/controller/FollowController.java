package com.niit.feed.controller;

import com.niit.feed.model.FollowRelation;
import com.niit.feed.service.FollowService;
import com.niit.feed.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    public FollowController(FollowService followService, UserService userService) {
        this.followService = followService;
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> follow(@RequestBody Map<String, Object> body) {
        Long userId = userService.getCurrentUserId();
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String action = (String) body.getOrDefault("action", "follow");

        if ("unfollow".equals(action)) {
            followService.unfollow(userId, targetUserId);
        } else {
            followService.follow(userId, targetUserId);
        }
        return Map.of("ok", true);
    }

    @GetMapping("/list")
    public List<FollowRelation> list() {
        Long userId = userService.getCurrentUserId();
        return followService.getFollowingList(userId);
    }
}
