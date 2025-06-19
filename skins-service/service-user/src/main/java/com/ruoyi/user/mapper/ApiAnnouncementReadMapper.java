package com.ruoyi.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiAnnouncementReadMapper {

    int countAnnouncementRead(@Param("announcementId") Integer announcementId, @Param("userId") Long userId);

    int addAnnouncementRead(@Param("announcementId") Integer announcementId, @Param("userId") Long userId);
}
