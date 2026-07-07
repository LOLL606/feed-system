package com.niit.feed.service;

import com.niit.feed.model.User;
import com.niit.feed.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private Long currentUserId = 1L;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> listAll() {
        return userMapper.findAll();
    }

    public User getCurrentUser() {
        return userMapper.findById(currentUserId);
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void switchUser(Long userId) {
        this.currentUserId = userId;
    }
}
