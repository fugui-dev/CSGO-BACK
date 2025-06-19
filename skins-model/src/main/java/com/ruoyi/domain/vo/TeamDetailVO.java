package com.ruoyi.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamDetailVO {
    private Integer employeeId;
    private Timestamp beginTime;
}
