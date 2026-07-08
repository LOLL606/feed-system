package com.niit.feed.mapper;

import com.niit.feed.model.FollowRelation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FollowRelationMapper {

    @Insert("INSERT INTO follow_relation (user_id, target_user_id) VALUES (#{userId}, #{targetUserId})")
    void insert(FollowRelation relation);

    @Delete("DELETE FROM follow_relation WHERE user_id = #{userId} AND target_user_id = #{targetUserId}")
    void delete(FollowRelation relation);

    @Select("SELECT * FROM follow_relation WHERE user_id = #{userId}")
    List<FollowRelation> findByUserId(Long userId);

    @Select("SELECT user_id FROM follow_relation WHERE target_user_id = #{targetUserId}")
    List<Long> findFollowerIds(Long targetUserId);

    @Select("SELECT target_user_id FROM follow_relation WHERE user_id = #{userId}")
    List<Long> findFollowingIds(Long userId);

    @Select("SELECT COUNT(*) FROM follow_relation WHERE user_id = #{userId}")
    int countFollowing(Long userId);

    @Select("SELECT COUNT(*) FROM follow_relation WHERE target_user_id = #{targetUserId}")
    int countFollowers(Long targetUserId);

    // TODO 1: 查"我"关注的大V
    @Select("SELECT fr.target_user_id FROM follow_relation fr JOIN `user` u ON fr.target_user_id = u.id WHERE fr.user_id = #{userId} AND u.is_big_v = 1")
    List<Long> findFollowedBigVIds(Long userId);

    // TODO 4: 查"我"关注的普通用户（非大V）
    @Select("SELECT fr.target_user_id FROM follow_relation fr JOIN `user` u ON fr.target_user_id = u.id WHERE fr.user_id = #{userId} AND u.is_big_v = 0")
    List<Long> findFollowedNonBigVIds(Long userId);
}
