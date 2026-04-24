package org.example.enterprisebacksystem.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.mapper.PermissionMapper;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
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
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        List<String> roles = permissionMapper.selectRoleCodesByUserId(user.getId());

        List<String> allAuthorities = new ArrayList<>();
        if (permissions != null) {
            allAuthorities.addAll(permissions);
        }

        if (roles != null) {
            roles.forEach(role -> {
                if (role != null && !role.isBlank()) {
                    allAuthorities.add("ROLE_" + role);
                }
            });
        }

        return new LoginUser(user, allAuthorities);
    }
}