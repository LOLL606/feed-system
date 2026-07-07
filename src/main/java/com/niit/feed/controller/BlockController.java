package com.niit.feed.controller;

import com.niit.feed.mapper.BlockRelationMapper;
import com.niit.feed.model.BlockRelation;
import com.niit.feed.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/block")
public class BlockController {

    private final BlockRelationMapper blockMapper;
    private final UserService userService;

    public BlockController(BlockRelationMapper blockMapper, UserService userService) {
        this.blockMapper = blockMapper;
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> block(@RequestBody Map<String, Object> body) {
        Long userId = userService.getCurrentUserId();
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String action = (String) body.getOrDefault("action", "block");

        if ("unblock".equals(action)) {
            BlockRelation relation = new BlockRelation();
            relation.setUserId(userId);
            relation.setTargetUserId(targetUserId);
            blockMapper.delete(relation);
            return Map.of("ok", true, "action", "unblock");
        } else {
            BlockRelation relation = new BlockRelation();
            relation.setUserId(userId);
            relation.setTargetUserId(targetUserId);
            blockMapper.insert(relation);
            return Map.of("ok", true, "action", "block");
        }
    }
}
