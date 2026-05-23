package org.example.enterprisebacksystem.dto.oa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OaProcessSaveReq {
    @NotBlank(message = "流程名称不能为空")
    @Size(max = 100, message = "流程名称最长100字符")
    private String title;

    @NotBlank(message = "流程编码不能为空")
    @Size(max = 50, message = "流程编码最长50字符")
    private String code;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    private Long ownerEmployeeId;

    @Size(max = 50, message = "状态文案最长50字符")
    private String statusText;

    @Size(max = 30, message = "色彩标识最长30字符")
    private String tone;

    private Integer pendingCount = 0;
    private Integer status = 1;
}
