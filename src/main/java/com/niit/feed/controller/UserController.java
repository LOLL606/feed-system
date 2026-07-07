package com.niit.feed.controller;

import com.niit.feed.service.UserService;
import com.niit.feed.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public List<User> list() {
        return userService.listAll();
    }

    @PostMapping("/switch")
    public User switchUser(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        userService.switchUser(userId);
        return userService.getCurrentUser();
    }

    @GetMapping("/profile")
    public User profile() {
        return userService.getCurrentUser();
    }
}
