package com.ruoyi.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.admin.service.TtBoxRecordsService;
import com.ruoyi.admin.service.TtOrnamentService;
import com.ruoyi.admin.service.TtOrnamentYYService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.domain.dto.queryCondition.OrnamentCondition;
import com.ruoyi.domain.entity.TtBoxRecords;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.domain.other.TtOrnamentsBody;
import com.ruoyi.domain.vo.TtOrnamentVO;
import com.ruoyi.domain.vo.upgrade.SimpleOrnamentVO;
import com.ruoyi.system.mapper.SysDictDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.domain.common.constant.TtboxRecordSource.SYS_GRANT;

@Service
public class TtOrnamentServiceImpl extends ServiceImpl<TtOrnamentMapper, TtOrnament> implements TtOrnamentService {

    @Autowired
    private TtBoxRecordsService boxRecordsService;

    @Autowired
    private TtOrnamentYYService ttOrnamentYYService;

    @Autowired
    private TtOrnamentMapper ttOrnamentMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Override
    public Page<TtOrnamentVO> listByParam(TtOrnamentsBody param) {

        LambdaQueryWrapper<TtOrnament> wrapper = new LambdaQueryWrapper<>();

        // id
        wrapper.eq(ObjectUtil.isNotEmpty(param.getId()), TtOrnament::getId, param.getId());

        // name
        wrapper
                .like(ObjectUtil.isNotEmpty(param.getItemName()), TtOrnament::getName, param.getItemName());

        // 外观
        wrapper.eq(ObjectUtil.isNotEmpty(param.getExterior()), TtOrnament::getExterior, param.getExterior());

        // 类型
        wrapper.eq(ObjectUtil.isNotEmpty(param.getType()), TtOrnament::getType, param.getType());

        // 价格区间
        wrapper
                .ge(ObjectUtil.isNotEmpty(param.getMinPrice()), TtOrnament::getUsePrice, param.getMinPrice())
                .le(ObjectUtil.isNotEmpty(param.getMaxPrice()), TtOrnament::getUsePrice, param.getMaxPrice());

        if (ObjectUtil.isNull(param.getPageNum())) {
            param.setPageNum(1);
        }
        if (ObjectUtil.isNull(param.getPageSize())) {
            param.setPageSize(10);
        }
        Page<TtOrnament> pageInfo = new Page<>(param.getPageNum(), param.getPageSize());
        pageInfo.setOptimizeCountSql(false);
        pageInfo = this.page(pageInfo, wrapper);

        List<TtOrnament> list = pageInfo.getRecords();

        List<TtOrnamentVO> res = list.stream().map(item -> {
            TtOrnamentVO vo = new TtOrnamentVO();
            BeanUtil.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        Page<TtOrnamentVO> resPage = new Page<>();
        BeanUtil.copyProperties(pageInfo, resPage);
        resPage.setRecords(res);

        return resPage;

    }

    @Override
    public List<TtOrnamentVO> selectTtOrnamentsList(TtOrnamentsBody param) {


        return null;

        // if (param.getPartyType().equals(PartyType.ZBT.getCode())){
        //
        //     LambdaQueryWrapper<TtOrnament> wrapper = Wrappers.lambdaQuery();
        //     if (ObjectUtils.isNotEmpty(param.getId())) wrapper.eq(TtOrnament::getId, param.getId());
        //     if (StringUtils.isNotNull(param.getName())) wrapper.like(TtOrnament::getName, param.getName());
        //     if (StringUtils.isNotNull(param.getTypeId())) wrapper.eq(TtOrnament::getType, param.getTypeId());
        //     if (StringUtils.isNotNull(param.getExterior())) wrapper.eq(TtOrnament::getExterior, param.getExterior());
        //     if (StringUtils.isNotNull(param.getMaxPrice())) wrapper.between(TtOrnament::getUsePrice, param.getMinPrice(), param.getMaxPrice());
        //     wrapper.eq(TtOrnament::getIsProprietaryProperty, "1");
        //     wrapper.orderByDesc(TtOrnament::getQuantity);
        //     List<TtOrnament> list = this.list(wrapper);
        //
        //     return list.stream().map(item->{
        //         CommonOrnamentVO build = CommonOrnamentVO.builder()
        //                 .build();
        //         BeanUtil.copyProperties(item,build);
        //         return build;
        //     }).collect(Collectors.toList());
        //
        // }else if (param.getPartyType().equals(PartyType.YY_YOU_PING.getCode())){
        //
        //     LambdaQueryWrapper<TtOrnamentsYY> wrapper = Wrappers.lambdaQuery();
        //     if (ObjectUtils.isNotEmpty(param.getId())) wrapper.eq(TtOrnamentsYY::getId, param.getId());
        //     if (StringUtils.isNotNull(param.getName())) wrapper.like(TtOrnamentsYY::getName, param.getName());
        //     if (StringUtils.isNotNull(param.getTypeId())) wrapper.eq(TtOrnamentsYY::getTypeId, param.getTypeId());
        //     if (StringUtils.isNotNull(param.getTypeName())) wrapper.eq(TtOrnamentsYY::getTypeName, param.getTypeName());
        //     // if (StringUtils.isNotNull(param.getMaxPrice())) wrapper.between(TtOrnamentsZBT::getUsePrice, param.getMinPrice(), param.getMaxPrice());
        //     wrapper.orderByDesc(TtOrnamentsYY::getId);
        //     List<TtOrnamentsYY> list = ttOrnamentYYService.list(wrapper);
        //
        //     return list.stream().map(item->{
        //         CommonOrnamentVO build = CommonOrnamentVO.builder()
        //                 .build();
        //         BeanUtil.copyProperties(item,build);
        //         return build;
        //     }).collect(Collectors.toList());
        //
        // }else {
        //     log.warn("非法的平台类型参数。");
        //     return null;
        // }

    }

    @Override
    public List<String> selectOrnamentsItemIdList() {
        return getBaseMapper().selectOrnamentsItemIdList();
    }

    @Override
    public AjaxResult grantOrnaments(Integer userId, Long ornamentId, Integer ornamentsLevelId, Integer num) {
        TtOrnament ttOrnament = new LambdaQueryChainWrapper<>(ttOrnamentMapper).eq(TtOrnament::getId, ornamentId).one();
        if (ObjectUtil.isEmpty(ttOrnament)) {
            return AjaxResult.error("不存在的饰品");
        }
        for (int i = 0; i < num; i++) {
            TtBoxRecords boxRecords = TtBoxRecords.builder().build();
            boxRecords.setUserId(userId);
            boxRecords.setOrnamentId(ornamentId);
            boxRecords.setOrnamentsPrice(ttOrnament.getUsePrice());
            boxRecords.setOrnamentsLevelId(ornamentsLevelId);
            boxRecords.setCreateTime(DateUtils.getNowDate());
            boxRecords.setSource(SYS_GRANT.getCode());
            boxRecords.setHolderUserId(userId);
            boxRecordsService.save(boxRecords);
        }
        return AjaxResult.success();
    }

    @Override
    public List<SimpleOrnamentVO> byCondition(OrnamentCondition condition) {
        // condition.setLimit((condition.getPageNum() - 1) * condition.getPageSize());
        return baseMapper.byCondition(condition);
        // Integer total = baseMapper.countByCondition(condition);
        //
        // Page<SimpleOrnamentVO> pageInfo = new Page<>(condition.getPageNum(), condition.getPageSize());
        // pageInfo.setRecords(list);
        // pageInfo.setTotal(total);
        //
        // return R.ok(pageInfo);
    }

    @Override
    public List<SimpleOrnamentVO> byCondition2(OrnamentCondition condition) {
        return baseMapper.byCondition2(condition);
    }

    @Override
    public List<Long> selectOrnamentsIdList() {
        return ttOrnamentMapper.selectOrnamentsIdList();
    }

    @Override
    public List<String> selectOrnamentsMarketHashNameList() {
        return ttOrnamentMapper.selectOrnamentsMarketHashNameList();
    }
}
