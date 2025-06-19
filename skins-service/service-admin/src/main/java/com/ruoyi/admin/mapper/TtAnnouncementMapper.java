package com.ruoyi.admin.mapper;

import com.ruoyi.domain.other.TtAnnouncement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtAnnouncementMapper {

    List<TtAnnouncement> getAnnouncementList();

    TtAnnouncement getAnnouncementByAnnouncementId(Integer announcementId);

    int addAnnouncement(TtAnnouncement ttAnnouncement);

    int editAnnouncement(TtAnnouncement ttAnnouncement);

    int removeAnnouncementByAnnouncementId(Integer announcementId);
}
