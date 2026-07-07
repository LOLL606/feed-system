package com.niit.feed.mapper;

import com.niit.feed.model.FeedOutbox;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedOutboxMapper {

    @Insert("INSERT INTO feed_outbox (user_id, post_id, content) VALUES (#{userId}, #{postId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FeedOutbox outbox);

    List<FeedOutbox> findByUserId(@Param("userId") Long userId,
                                  @Param("cursor") Long cursor,
                                  @Param("size") int size);

    // 批量查多个大V的 outbox（getTimeline 用）
    List<FeedOutbox> findByUserIds(@Param("userIds") List<Long> userIds,
                                   @Param("cursor") Long cursor,
                                   @Param("size") int size);
}
