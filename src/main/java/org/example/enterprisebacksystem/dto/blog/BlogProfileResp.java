package org.example.enterprisebacksystem.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BlogProfileResp {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private Integer siteOwner;
    private LocalDateTime createTime;
    private List<String> roles;
    private List<String> permissions;
}
