package com.niit.feed.mapper;

import com.niit.feed.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM `user` ORDER BY id")
    List<User> findAll();

    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Update("UPDATE `user` SET follower_count = follower_count + #{delta} WHERE id = #{userId}")
    void updateFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

    // TODO 5: 查24h内活跃的粉丝（发帖时只推送给这些人）
    @Select("SELECT fr.user_id FROM follow_relation fr JOIN `user` u ON fr.user_id = u.id WHERE fr.target_user_id = #{targetUserId} AND u.last_active_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    List<Long> findActiveFollowerIds(Long targetUserId);

    @Update("UPDATE `user` SET last_active_at = NOW() WHERE id = #{userId}")
    void updateLastActiveAt(@Param("userId") Long userId);

    @Update("UPDATE `user` SET is_big_v = 1 WHERE id = #{userId}")
    void updateBigV(@Param("userId") Long userId);
}
