package org.example.enterprisebacksystem.dto.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogArticleSaveReq {
    @NotBlank(message = "标题不能为空")
    @Size(max = 160, message = "标题最长160字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 80, message = "作者最长80字符")
    private String author;

    @Size(max = 500, message = "摘要最长500字符")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String contentMarkdown;

    @Size(max = 50, message = "标签最长50字符")
    private String tag;

    @Size(max = 80, message = "分类最长80字符")
    private String category;

    private Integer status = 1;
}
