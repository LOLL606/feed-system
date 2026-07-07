package com.niit.feed.mapper;

import com.niit.feed.model.FeedInbox;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FeedInboxMapper {

    void batchInsert(@Param("list") List<FeedInbox> list);

    @Insert("INSERT INTO feed_inbox (user_id, post_id, from_user_id) VALUES (#{userId}, #{postId}, #{fromUserId})")
    void insert(FeedInbox inbox);

    List<FeedInbox> findByUserId(@Param("userId") Long userId,
                                 @Param("cursor") Long cursor,
                                 @Param("size") int size);

    @Update("UPDATE feed_inbox SET is_pinned = 1 WHERE user_id = #{userId} AND post_id = #{postId}")
    void pinPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Update("UPDATE feed_inbox SET is_pinned = 0 WHERE user_id = #{userId} AND post_id = #{postId}")
    void unpinPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Delete("DELETE FROM feed_inbox WHERE post_id IN " +
            "(SELECT id FROM feed_post WHERE visible_until IS NOT NULL AND visible_until < NOW())")
    int deleteExpiredInbox();
}
