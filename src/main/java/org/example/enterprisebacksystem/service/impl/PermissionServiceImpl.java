package org.example.enterprisebacksystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.enterprisebacksystem.domain.Permission;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import org.example.enterprisebacksystem.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
