package com.ruoyi.admin.service;

import com.ruoyi.domain.other.TtAnnouncement;

import java.util.List;

public interface TtAnnouncementService {

    List<TtAnnouncement> getAnnouncementList();

    TtAnnouncement getAnnouncementByAnnouncementId(Long userId, Integer announcementId);

    int addAnnouncement(TtAnnouncement ttAnnouncement);

    int editAnnouncement(TtAnnouncement ttAnnouncement);

    int removeAnnouncementByAnnouncementId(Integer announcementId);
}
