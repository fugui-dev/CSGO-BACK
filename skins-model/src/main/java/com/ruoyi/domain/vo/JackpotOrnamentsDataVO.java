package com.ruoyi.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JackpotOrnamentsDataVO {
    //产品名称
    private String itemName;
    //成色
    private String exteriorName;
    //图片地址
    private String imageUrl;
    //等级
    private String level;
    //等级图片
    private String levelImg;
    //商品名称
    private String shortName;
    //类型名称
    private String typeName;
    //价格
    private BigDecimal usePrice;
}
