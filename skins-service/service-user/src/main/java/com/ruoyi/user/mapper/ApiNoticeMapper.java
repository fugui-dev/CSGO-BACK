package com.ruoyi.user.mapper;

import com.ruoyi.domain.other.TtNotice;
import com.ruoyi.user.model.vo.ApiNoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiNoticeMapper {

    List<ApiNoticeVO> getNoticeList(Long userId);

    ApiNoticeVO getNoticeByNoticeId(@Param("userId") Long userId, @Param("noticeId") Integer noticeId);

    int countUnreadNotice(Long userId);

    int addNotice(TtNotice ttNotice);

    int editNotice(TtNotice ttNotice);

    int removeNoticeByNoticeId(Integer noticeId);
}
