package org.example.enterprisebacksystem.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.List;
import java.util.stream.Collectors;
import org.example.enterprisebacksystem.domain.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {
    private User user;

    /**
     * 这里保存权限字符串：
     * 例如 user:list、user:create、ROLE_admin
     */
    private List<String> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // 用户的用户名和密码
    // 帐号的异常状态
    //
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == 1;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getUserId() {
        return user == null ? null : user.getId();
    }
}
