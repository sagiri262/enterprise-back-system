package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.BlogArticle;
import org.example.enterprisebacksystem.mapper.BlogArticleMapper;
import org.example.enterprisebacksystem.service.BlogArticleService;
import org.springframework.stereotype.Service;

@Service
public class BlogArticleServiceImpl extends ServiceImpl<BlogArticleMapper, BlogArticle> implements BlogArticleService {
}
