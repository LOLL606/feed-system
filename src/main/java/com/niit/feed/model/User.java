package com.niit.feed.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String name;
    private String avatar;
    private Boolean isBigV;
    private Integer followerCount;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
