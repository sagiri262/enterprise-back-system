package org.example.enterprisebacksystem.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

import org.example.enterprisebacksystem.common.utils.JwtUtils;
import org.example.enterprisebacksystem.common.annotation.AuditLog;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.User;
import org.example.enterprisebacksystem.dto.auth.LoginReq;
import org.example.enterprisebacksystem.dto.auth.LoginResp;
import org.example.enterprisebacksystem.service.AuthService;
import org.example.enterprisebacksystem.service.UserService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        // 获取 tokens
        // 从 Header:Authorization
        String token = request.getHeader("Authorization");
        if(!StringUtils.hasText(token)) {
            // 如果没有
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 解析 tokens
            Claims claims = JwtUtils.parseToken(token);
            // 用户名获取对象
            String username = claims.getSubject();

            // 封装 Authencation 对象存入上下文中
            UserDetails userDetails = userDetailService.loadUserByUsername(username);
            // new 一个新的对象
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        catch(Exception e) {
            // Token 无效或过期，这里暂不处理，交给 Security 后续过滤器拦截
        }

        filterChain.doFilter(request, response);
    }
}
