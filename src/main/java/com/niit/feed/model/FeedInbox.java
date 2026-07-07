package com.niit.feed.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedInbox {
    private Long id;
    private Long userId;
    private Long postId;
    private Long fromUserId;
    private Boolean isPinned;
    private LocalDateTime createdAt;

    // 关联查询字段
    private String content;
    private String fromUserName;
}
