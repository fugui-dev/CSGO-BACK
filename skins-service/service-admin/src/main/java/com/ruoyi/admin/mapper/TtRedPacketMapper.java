package com.ruoyi.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.domain.other.TtRedPacket;
import com.ruoyi.domain.other.TtRedPacketBody;
import com.ruoyi.domain.vo.TtRedPacketDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TtRedPacketMapper extends BaseMapper<TtRedPacket> {
    List<TtRedPacketDataVO> queryList(TtRedPacketBody ttRedPacketBody);
}
