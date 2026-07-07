package com.niit.feed.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedOutbox {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt;

    // JOIN 查询填充的字段
    private String content;
    private String userName;
}
