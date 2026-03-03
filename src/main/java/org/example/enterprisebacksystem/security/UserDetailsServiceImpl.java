package org.example.enterprisebacksystem.security;

import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<>().eq(User::getUsername, username));
        // 抛出异常
        if(user == null) throw new UsernameNotFoundException("用户不存在");
        // 查询角色
        // 查询角色当前的权限
        List<String> perms = permissionMapper.selectPermissionCodesByUserId(user.getId());
        List<String> roles = permissionMapper.selectRoleCodesByUserId(user.getId());

        // 权限添加到数组里
        List<String> allAuthorities = new ArrayList<>();
        // Spring Security 角色建议加 ROLE_ 前缀
        roles.forEach(role -> allAuthorities.add("ROLE_" + role));

        return new LoginUser(user, allAuthorities);

    }
}
