package org.example.enterprisebacksystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_oa_process")
public class OaProcess extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String code;
    private String category;
    private Long ownerEmployeeId;
    private String statusText;
    private String tone;
    private Integer pendingCount;
    private Integer status;
}
