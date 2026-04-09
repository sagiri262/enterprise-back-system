package org.example.enterprisebacksystem.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    // 操作描述，如 "新增用户"
    String value() default "";
}
