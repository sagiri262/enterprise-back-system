package org.example.enterprisebacksystem.dto.demo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemoCreateReq {
    @NotBlank(message = "name 不能为空")
    @Size(max = 20, message = "name 最长不超过20字符")
    private String name;
}
