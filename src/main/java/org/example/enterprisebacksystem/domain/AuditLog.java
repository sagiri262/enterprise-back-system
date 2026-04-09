package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {
    @TableId(type=IdType.AUTO)
    /*
    * ID
    * USERNAME
    * USERID*/
    private Long id;
    private Long userId;
    private String userName;
    /*
    * 操作方法 action
    * 请求方法（GET/POST）
    * 请求路径 url
    * 请求参数 params
    * IP地址
    * 状态 1和0表示成功与否
    * 耗时计算 单位：ms
    * */
    private String action;
    private String method;
    private String url;
    private String params;
    private String ipAddress;
    private Integer status;
    private Long costTime;
    private LocalDateTime createTime;
}
