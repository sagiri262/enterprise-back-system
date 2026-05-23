package org.example.enterprisebacksystem.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeSaveReq {
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题最长120字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容最长2000字符")
    private String content;

    @Size(max = 30, message = "级别最长30字符")
    private String level;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    private Integer status = 1;
}
