package com.ruoyi.thirdparty.zbt.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckSteam {
    private Integer appId;//	游戏id,示例值(570)	query	true integer(int32)

    private String tradeUrl;//	CSGO类别	query	false string

    private Integer type;//	csgo外观	query	false string



}
