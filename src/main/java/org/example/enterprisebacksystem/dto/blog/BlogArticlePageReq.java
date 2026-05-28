package org.example.enterprisebacksystem.dto.blog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BlogArticlePageReq {
    @Min(value = 1, message = "页码最小为1")
    private long page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private long size = 10;

    private String keyword;
}
