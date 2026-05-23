package org.example.enterprisebacksystem.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 *通用分页请求基类
 * 所有分页查询DTO都可以继承它
 * */

@Data
public class PageReq {
    @Min(value = 1, message = "页码最小为 1")
    private long page = 1;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private long size = 10;
}
