package com.niit.feed.mapper;

import com.niit.feed.model.BlockRelation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BlockRelationMapper {

    @Select("SELECT target_user_id FROM block_relation WHERE user_id = #{userId}")
    List<Long> findBlockedUserIds(@Param("userId") Long userId);

    @Insert("INSERT INTO block_relation (user_id, target_user_id) VALUES (#{userId}, #{targetUserId})")
    void insert(BlockRelation relation);

    @Delete("DELETE FROM block_relation WHERE user_id = #{userId} AND target_user_id = #{targetUserId}")
    void delete(BlockRelation relation);
}
