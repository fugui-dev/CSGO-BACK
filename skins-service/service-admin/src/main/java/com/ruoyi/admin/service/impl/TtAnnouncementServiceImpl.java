package com.ruoyi.admin.service.impl;

import com.ruoyi.admin.mapper.TtAnnouncementMapper;
import com.ruoyi.admin.service.TtAnnouncementService;
import com.ruoyi.domain.other.TtAnnouncement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TtAnnouncementServiceImpl implements TtAnnouncementService {

    @Autowired
    private TtAnnouncementMapper ttAnnouncementMapper;

    @Override
    public List<TtAnnouncement> getAnnouncementList() {
        return ttAnnouncementMapper.getAnnouncementList();
    }

    @Override
    public TtAnnouncement getAnnouncementByAnnouncementId(Long userId, Integer announcementId) {
        return ttAnnouncementMapper.getAnnouncementByAnnouncementId(announcementId);
    }

    @Override
    public int addAnnouncement(TtAnnouncement ttAnnouncement) {
        ttAnnouncement.setCreateTime(new Date());
        return ttAnnouncementMapper.addAnnouncement(ttAnnouncement);
    }

    @Override
    public int editAnnouncement(TtAnnouncement ttAnnouncement) {
        return ttAnnouncementMapper.editAnnouncement(ttAnnouncement);
    }

    @Override
    public int removeAnnouncementByAnnouncementId(Integer announcementId) {
        return ttAnnouncementMapper.removeAnnouncementByAnnouncementId(announcementId);
    }
}
