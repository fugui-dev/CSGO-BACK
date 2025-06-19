package com.ruoyi.domain.other;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 福利列表对象 tt_welfare
 *
 * @author ruoyi
 * @date 2024-05-11
 */
@Data
public class TtWelfare extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 福利ID */
    private Integer welfareId;

    /** 福利名称 */
    private String welfareName;

    /** 类型 */
    private String type;

    /** VIP等级 */
    private Integer vipLevel;

    /** 箱子ID */
    private Integer boxId;
}
