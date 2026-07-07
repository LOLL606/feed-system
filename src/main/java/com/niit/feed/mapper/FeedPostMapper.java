package com.niit.feed.mapper;

import com.niit.feed.model.FeedPost;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FeedPostMapper {

    @Insert("INSERT INTO feed_post (user_id, content, visible_until) VALUES (#{userId}, #{content}, #{visibleUntil})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FeedPost post);

    @Select("SELECT * FROM feed_post WHERE id = #{id}")
    FeedPost findById(Long id);

    @Select("SELECT p.*, u.name AS user_name FROM feed_post p JOIN `user` u ON p.user_id = u.id WHERE p.id = #{id}")
    FeedPost findByIdWithUser(Long id);

    @Select("SELECT * FROM feed_post WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<FeedPost> findByUserId(Long userId);

    List<FeedPost> findMissedPosts(@Param("userId") Long userId,
                                    @Param("followedUserIds") List<Long> followedUserIds,
                                    @Param("since") LocalDateTime since,
                                    @Param("size") int size);
}
