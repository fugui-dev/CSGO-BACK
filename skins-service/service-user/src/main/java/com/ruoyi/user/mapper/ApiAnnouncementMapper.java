package com.ruoyi.user.mapper;

import com.ruoyi.domain.other.TtAnnouncement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiAnnouncementMapper {

    List<TtAnnouncement> getAnnouncementList(Long userId);

    TtAnnouncement getAnnouncementByAnnouncementId(Integer announcementId);

    int countUnreadAnnouncement(Long userId);
}
