package com.ruoyi.user.service;

import com.ruoyi.domain.other.TtNotice;
import com.ruoyi.user.model.vo.ApiNoticeVO;

import java.util.List;

public interface ApiNoticeService {

    List<ApiNoticeVO> getNoticeList(Long userId);

    ApiNoticeVO getNoticeByNoticeId(Long userId, Integer noticeId);

    int countUnreadNotice(Long userId);

    int addNotice(TtNotice ttNotice);

    int editNotice(TtNotice ttNotice);

    int removeNoticeByNoticeId(Integer noticeId);
}
