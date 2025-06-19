package com.ruoyi.user.model.vo;

import com.ruoyi.domain.other.TtVipLevel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VipLevelInfoVO {

    @ApiModelProperty("总充值")
    private BigDecimal totalRecharge;

    @ApiModelProperty("所有会员等级，参考管理后台VIP等级设置所展示数据")
    private List<TtVipLevel> vipLevelList;

}
