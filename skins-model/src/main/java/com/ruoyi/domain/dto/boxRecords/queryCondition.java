package com.ruoyi.domain.dto.boxRecords;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class queryCondition {

    private Long boxRecordId;

    // 宝箱id
    @ApiModelProperty("某个宝箱的出货记录，不传ID时为所有宝箱出货记录")
    private Integer boxId;

    // 用户类型
    private String userType;

    // 来源
    @ApiModelProperty("来源（1开箱 2对战 3Roll房 4百分比幸运升级 5管理员发放 6商城兑换 7汰换 8注册赠送）")
    private List<Integer> source;

    // 物品价格区间
    @Min(value = 0,message = "最小0")
    @ApiModelProperty("最小物品价格")
    private BigDecimal ornamentPriceMin;
    @Min(value = 0,message = "最小0")
    @ApiModelProperty("最大物品价格")
    private BigDecimal ornamentPriceMax;

    // 物品等级
    private List<Integer> ornamentLevelIds;

    // 状态
    private List<Integer> status;

    // 0时间升序 1时间降序
    @ApiModelProperty("排序规则：0时间升序 1时间降序")
    private Integer orderByFie;

    @Min(value = 1,message = "最小1")
    @ApiModelProperty("页码")
    private Integer page;

    @Range(min = 1,max = 20)
    @ApiModelProperty("分页大小")
    private Integer size;

    @ApiModelProperty(hidden = true)
    private Integer limit;

    @ApiModelProperty(hidden = true)
    private Integer userId;
}
