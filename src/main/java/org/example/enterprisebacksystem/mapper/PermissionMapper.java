package org.example.enterprisebacksystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.enterprisebacksystem.domain.Permission;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    // 根据用户 ID 查询权限编码列表 (如: "user:list", "user:add")
    @Select("SELECT DISTINCT p.code " +
            "FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.code IS NOT NULL")
    List<String> selectPermissionCodesByUserId(Long userId);


    // 根据用户 ID 查询权限编码列表 (如: "ROLE_ADMIN")
    @Select("select distinct r.code" +
            "from sys_role r" +
            "inner join sys_user_role ur on r.id = ur.role_id" +
            "where ur.user_id = #{userId}")


    List<String> selectRoleCodesByUserId(Long userId);
}
