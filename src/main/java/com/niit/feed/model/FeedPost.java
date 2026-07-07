package com.niit.feed.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedPost {
    private Long id;
    private Long userId;
    private String content;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime visibleUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段
    private String userName;
    private String source;
}
