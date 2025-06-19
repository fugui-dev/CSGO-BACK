package com.ruoyi.user.service.impl;

import com.ruoyi.domain.other.TtAnnouncement;
import com.ruoyi.user.mapper.ApiAnnouncementMapper;
import com.ruoyi.user.mapper.ApiAnnouncementReadMapper;
import com.ruoyi.user.service.ApiAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiAnnouncementServiceImpl implements ApiAnnouncementService {

    @Autowired
    private ApiAnnouncementMapper apiAnnouncementMapper;

    @Autowired
    private ApiAnnouncementReadMapper apiAnnouncementReadMapper;

    @Override
    public List<TtAnnouncement> getAnnouncementList(Long userId) {
        return apiAnnouncementMapper.getAnnouncementList(userId);
    }

    @Override
    public TtAnnouncement getAnnouncementByAnnouncementId(Integer announcementId, Long userId) {
        // 新增用户和公告关联，表示已读
        if (apiAnnouncementReadMapper.countAnnouncementRead(announcementId, userId) == 0) {
            apiAnnouncementReadMapper.addAnnouncementRead(announcementId, userId);
        }
        return apiAnnouncementMapper.getAnnouncementByAnnouncementId(announcementId);
    }

    @Override
    public int countUnreadAnnouncement(Long userId) {
        return apiAnnouncementMapper.countUnreadAnnouncement(userId);
    }
}
