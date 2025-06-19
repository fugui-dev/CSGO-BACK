package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtRedPacketRecord;
import com.ruoyi.domain.other.TtRedPacketRecordBody;
import com.ruoyi.domain.vo.TtRedPacketRecordDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtRedPacketRecordMapper extends BaseMapper<TtRedPacketRecord> {
    List<TtRedPacketRecordDataVO> queryList(TtRedPacketRecordBody ttRedPacketRecordBody);
}
