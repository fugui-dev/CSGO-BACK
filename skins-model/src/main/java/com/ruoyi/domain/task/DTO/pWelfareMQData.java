package com.ruoyi.domain.task.DTO;

import com.ruoyi.domain.entity.sys.TtUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class pWelfareMQData implements Serializable {

    // 消费者
    private Integer userId;
    private TtUser user;

    // 金额
    private BigDecimal account;

    // 消费类型
    private Integer accountType;

    private Timestamp createTime;

}
