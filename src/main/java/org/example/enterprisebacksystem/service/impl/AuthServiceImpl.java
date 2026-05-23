package org.example.enterprisebacksystem.service.impl;

// 导入本地的 接口interface 和 类class
import org.example.enterprisebacksystem.common.api.ErrorCode;
import org.example.enterprisebacksystem.common.exception.BizException;
import org.example.enterprisebacksystem.common.utils.JwtUtils;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.mapper.UserMapper;
import org.example.enterprisebacksystem.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResp login(LoginReq req) {
        // 根据用户名查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername())
                .last("LIMIT 1"));

        if(user == null) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "用户名或密码错误！");
        }

        // 校验账号状态
        if(user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "用户账号已被禁用！");
        }

        if(user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "账号密码未初始化，请联系管理员！");
        }

        // 3、校验密码
        // req.getPassword() 是明文，user.getPassword() 是数据库里的密文
        boolean matches;
        try {
            matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "数据库密码格式不是 BCrypt，请重新初始化或重置密码！");
        }

        if(!matches) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "用户名或密码错误！");
        }

        // 密码正确，生成 JWT token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());
        return new LoginResp(token, user.getId(), user.getUsername());
    }
}
