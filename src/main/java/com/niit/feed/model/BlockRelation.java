package com.niit.feed.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlockRelation {
    private Long id;
    private Long userId;
    private Long targetUserId;
    private LocalDateTime createdAt;
}
