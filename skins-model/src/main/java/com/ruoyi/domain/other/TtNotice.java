package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 通知实体类
 */
@Data
public class TtNotice {

    private Integer noticeId;

    private Long userId;

    private String title;

    private String content;

    private String read;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
