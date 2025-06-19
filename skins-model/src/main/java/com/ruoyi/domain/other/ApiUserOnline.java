package com.ruoyi.domain.other;

import lombok.Data;

@Data
public class ApiUserOnline {

    private String token;

    private String nickName;

    private String userName;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    private Long loginTime;
}
