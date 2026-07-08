package com.niit.feed.service;

import com.niit.feed.mapper.FollowRelationMapper;
import com.niit.feed.mapper.UserMapper;
import com.niit.feed.model.FollowRelation;
import com.niit.feed.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowService {

    private final FollowRelationMapper followMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final FeedService feedService;

    public FollowService(FollowRelationMapper followMapper,
                          UserMapper userMapper,
                          UserService userService,
                          FeedService feedService) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.feedService = feedService;
    }

    @Transactional
    public void follow(Long userId, Long targetUserId) {
        FollowRelation relation = new FollowRelation();
        relation.setUserId(userId);
        relation.setTargetUserId(targetUserId);
        followMapper.insert(relation);
        userMapper.updateFollowerCount(targetUserId, 1);

        checkBigVPromotion(targetUserId);
    }

    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        FollowRelation relation = new FollowRelation();
        relation.setUserId(userId);
        relation.setTargetUserId(targetUserId);
        followMapper.delete(relation);
        userMapper.updateFollowerCount(targetUserId, -1);
    }

    public List<FollowRelation> getFollowingList(Long userId) {
        return followMapper.findByUserId(userId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return followMapper.findFollowingIds(userId);
    }

    private void checkBigVPromotion(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null || user.getIsBigV()) return;
        if (user.getFollowerCount() >= feedService.getBigVThreshold()) {
            feedService.promoteToBigV(userId);
        }
    }
}
