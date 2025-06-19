package com.ruoyi.user.service;

import com.ruoyi.domain.other.TtAnnouncement;

import java.util.List;

public interface ApiAnnouncementService {

    List<TtAnnouncement> getAnnouncementList(Long userId);

    TtAnnouncement getAnnouncementByAnnouncementId(Integer announcementId, Long userId);

    int countUnreadAnnouncement(Long userId);
}
