package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.service.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
