package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article")
public class BlogArticle extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String summary;
    private String contentMarkdown;
    private String tag;
    private String category;
    private Integer status;
    private Integer readCount;
    private Integer commentCount;
    private LocalDateTime publishTime;
}
