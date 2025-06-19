package com.ruoyi.domain.other;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TtOrnamentsBody {

    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    private String typeName;

    /**
     * 外观
     */
    private String exterior;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    private Integer pageNum;

    private Integer pageSize;

    private Integer zbtId;

    private Integer yyyoupingId;

    private String itemName;
}
