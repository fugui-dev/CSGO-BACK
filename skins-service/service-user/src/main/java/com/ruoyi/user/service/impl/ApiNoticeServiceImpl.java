package com.ruoyi.user.service.impl;

import com.ruoyi.domain.other.TtNotice;
import com.ruoyi.user.mapper.ApiNoticeMapper;
import com.ruoyi.user.model.vo.ApiNoticeVO;
import com.ruoyi.user.service.ApiNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApiNoticeServiceImpl implements ApiNoticeService {

    @Autowired
    private ApiNoticeMapper apiNoticeMapper;

    @Override
    public List<ApiNoticeVO> getNoticeList(Long userId) {
        return apiNoticeMapper.getNoticeList(userId);
    }

    @Override
    public ApiNoticeVO getNoticeByNoticeId(Long userId, Integer noticeId) {
        // 读取详情后更新为已读状态
        TtNotice ttNotice = new TtNotice();
        ttNotice.setNoticeId(noticeId);
        ttNotice.setRead("1");
        editNotice(ttNotice);
        return apiNoticeMapper.getNoticeByNoticeId(userId, noticeId);
    }

    @Override
    public int countUnreadNotice(Long userId) {
        return apiNoticeMapper.countUnreadNotice(userId);
    }

    @Override
    public int addNotice(TtNotice ttNotice) {
        ttNotice.setCreateTime(new Date());
        return apiNoticeMapper.addNotice(ttNotice);
    }

    @Override
    public int editNotice(TtNotice ttNotice) {
        return apiNoticeMapper.editNotice(ttNotice);
    }

    @Override
    public int removeNoticeByNoticeId(Integer noticeId) {
        return apiNoticeMapper.removeNoticeByNoticeId(noticeId);
    }
}
