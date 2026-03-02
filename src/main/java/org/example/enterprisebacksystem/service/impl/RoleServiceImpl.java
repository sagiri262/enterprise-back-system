package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.Role;
import org.example.enterprisebacksystem.mapper.RoleMapper;
import org.example.enterprisebacksystem.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
