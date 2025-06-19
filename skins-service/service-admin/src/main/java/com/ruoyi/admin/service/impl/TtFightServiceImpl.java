package com.ruoyi.admin.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.domain.other.TtBox;
import com.ruoyi.domain.entity.fight.TtFight;
import com.ruoyi.admin.mapper.TtBoxMapper;
import com.ruoyi.admin.mapper.TtFightMapper;
import com.ruoyi.admin.service.TtFightService;
import com.ruoyi.domain.vo.FightBoxDataVO;
import com.ruoyi.domain.other.TtFightBody;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.domain.other.BoxDataBodyA;
import com.ruoyi.domain.vo.fight.FightBoxVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TtFightServiceImpl extends ServiceImpl<TtFightMapper, TtFight> implements TtFightService {

    private final TtBoxMapper boxMapper;

    private final TtFightMapper ttFightMapper;

    public TtFightServiceImpl(TtBoxMapper boxMapper,TtFightMapper ttFightMapper) {
        this.boxMapper = boxMapper;
        this.ttFightMapper = ttFightMapper;
    }

    @Override
    public List<TtFight> selectFightList(TtFightBody ttFightBody) {
        LambdaQueryWrapper<TtFight> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttFightBody.getId())) wrapper.eq(TtFight::getId, ttFightBody.getId());
        if (StringUtils.isNotEmpty(ttFightBody.getStatus())) wrapper.eq(TtFight::getStatus, ttFightBody.getStatus());
        if (StringUtils.isNotNull(ttFightBody.getUserId())) wrapper.eq(TtFight::getUserId, ttFightBody.getUserId());
        return this.list(wrapper);
    }

    // @Override
    // public List<FightBoxDataVO> selectFightBoxList(Integer fightId) {
    //     TtFight ttFight = this.getById(fightId);
    //     String boxData = JSON.toJSONString(ttFight.getBoxData());
    //     List<BoxDataBodyA> boxDataBodyAList = JSONObject.parseObject(boxData, new TypeReference<List<BoxDataBodyA>>() {
    //     });
    //     List<FightBoxDataVO> resultList = new ArrayList<>();
    //     for (BoxDataBodyA boxDataBodyA : boxDataBodyAList) {
    //         System.err.println(boxDataBodyA);
    //         // 这里输出的全是BoxDataBodyA(boxId=null, boxNum=null, boxName=null, boxImg01=null, boxImg02=null)
    //         TtBox ttBox = new LambdaQueryChainWrapper<>(boxMapper).eq(TtBox::getBoxId, boxDataBodyA.getBoxId()).one();
    //         for (int i = 0; i < boxDataBodyA.getBoxNum(); i++) {
    //             FightBoxDataVO fightBoxDataVO = FightBoxDataVO.builder().build();
    //             BeanUtils.copyBeanProp(fightBoxDataVO, ttBox);
    //             resultList.add(fightBoxDataVO);
    //         }
    //     }
    //     return resultList;
    // }

    @Override
    public List<FightBoxDataVO> selectFightBoxList(Integer fightId) {
        TtFight ttFight = this.getById(fightId);
        String boxDataJson = JSON.toJSONString(ttFight.getBoxData());

        // 解析 JSON 字符串为 Map<Integer, BoxDataBodyA>
        Map<Integer, BoxDataBodyA> boxDataMap = JSONObject.parseObject(boxDataJson, new TypeReference<Map<Integer, BoxDataBodyA>>() {});

        List<FightBoxDataVO> resultList = new ArrayList<>();
        boxDataMap.forEach((boxId, boxDataBodyA) -> {

            // 根据 boxId 查询对应的 TtBox 对象
            TtBox ttBox = new LambdaQueryChainWrapper<>(boxMapper)
                    .eq(TtBox::getBoxId, boxDataBodyA.getBoxId())
                    .one();

            if (ttBox != null) {
                // 根据 boxNum 创建多个 FightBoxDataVO 对象
                for (int i = 0; i < boxDataBodyA.getNumber(); i++) {
                    FightBoxDataVO fightBoxDataVO = new FightBoxDataVO();
                    BeanUtils.copyProperties(ttBox, fightBoxDataVO);
                    resultList.add(fightBoxDataVO);
                }
            }
        });

        return resultList;
    }

    @Override
    public int endFight(String fightId) {
        TtFight ttFight = ttFightMapper.selectOne(new QueryWrapper<TtFight>().eq("fight_id", fightId));
        ttFight.setStatus(2);
        return ttFightMapper.updateById(ttFight);
    }
}
