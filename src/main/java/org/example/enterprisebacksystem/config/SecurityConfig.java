package org.example.enterprisebacksystem.config;

import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.security.JwtAuthenticationTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启接口权限注解，如 @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF（前后端分离不需要）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用 Session
                /*
                 * "/api/auth/**",
                 * "/swagger-ui/**",
                 * "/v3/api-docs/**"
                * */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                         "/swagger-ui.html",
                                         "/swagger-ui/**",
                                         "/v3/api-docs",
                                         "/v3/api-docs/**").permitAll() // 白名单
                        .anyRequest().authenticated() // 其余接口必须登录
                );

        // 将我们的 JWT 过滤器放在系统账号密码过滤器之前
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
