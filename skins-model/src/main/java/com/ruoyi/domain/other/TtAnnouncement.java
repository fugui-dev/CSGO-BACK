package com.ruoyi.domain.other;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 公告实体类
 */
@Data
public class TtAnnouncement {

    private Integer AnnouncementId;

    private String title;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String read;
}
