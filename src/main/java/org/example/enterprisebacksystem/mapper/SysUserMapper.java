package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.SysUser;

// 继承 BaseMapper 后，即拥有 CRUD 能力
public interface SysUserMapper extends BaseMapper<SysUser> {
}
