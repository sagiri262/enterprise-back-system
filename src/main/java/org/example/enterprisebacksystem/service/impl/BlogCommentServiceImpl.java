package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.BlogComment;
import org.example.enterprisebacksystem.mapper.BlogCommentMapper;
import org.example.enterprisebacksystem.service.BlogCommentService;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment> implements BlogCommentService {
}
