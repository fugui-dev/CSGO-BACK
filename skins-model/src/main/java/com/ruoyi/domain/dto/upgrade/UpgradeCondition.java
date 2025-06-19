package com.ruoyi.domain.dto.upgrade;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpgradeCondition {

    private Long upgradeRecordId;

    //@NotNull(message = "升级饰品id不能为空。")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ornamentId;
    private String userType;
    private Integer userId;

    @Min(value = 1,message = "页码最小1")
    private Integer page;
    @Range(min = 1,max = 20)
    private Integer size;

    private Integer limit;

    private String targetItemName;
}
