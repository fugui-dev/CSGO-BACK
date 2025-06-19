package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.admin.service.ShoppingService;
import com.ruoyi.domain.other.ShoppingBody;
import com.ruoyi.domain.vo.ShoppingDataVO;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.page.PageDataInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.common.utils.PageUtils.startPage;

@Service
@Slf4j
public class ShoppingServiceImpl implements ShoppingService {

    private final ISysConfigService sysConfigService;
    private final TtOrnamentMapper ornamentsMapper;

    public ShoppingServiceImpl(ISysConfigService sysConfigService,
                               TtOrnamentMapper ornamentsMapper) {
        this.sysConfigService = sysConfigService;
        this.ornamentsMapper = ornamentsMapper;
    }

    @Override
    public PageDataInfo<ShoppingDataVO> list(ShoppingBody shoppingBody) {

        LambdaQueryChainWrapper<TtOrnament> wrapper = new LambdaQueryChainWrapper<>(ornamentsMapper);
        if (StringUtils.isNotNull(shoppingBody.getItemName()))
            wrapper.like(TtOrnament::getName, shoppingBody.getItemName());
        if (ObjectUtils.isNotEmpty(shoppingBody.getId())) wrapper.eq(TtOrnament::getId, shoppingBody.getId());
        if (StringUtils.isNotNull(shoppingBody.getExterior()))
            wrapper.eq(TtOrnament::getExterior, shoppingBody.getExterior());
        if (StringUtils.isNotNull(shoppingBody.getType())) wrapper.eq(TtOrnament::getType, shoppingBody.getType());
        if (StringUtils.isNotNull(shoppingBody.getMaxPrice()))
            wrapper.between(TtOrnament::getUsePrice, shoppingBody.getMinPrice(), shoppingBody.getMaxPrice());
        if (StringUtils.isNotNull(shoppingBody.getIsPutaway()))
            wrapper.eq(TtOrnament::getIsPutaway, shoppingBody.getIsPutaway());
        wrapper.eq(TtOrnament::getIsProprietaryProperty, "1");
        wrapper.orderByAsc(TtOrnament::getIsPutaway);

        startPage();
        List<TtOrnament> list = wrapper.list();

        String exchangePriceRatio = sysConfigService.selectConfigByKey("exchangePriceRatio");
        List<ShoppingDataVO> resultList = list.stream().map(ttOrnaments -> {
            ShoppingDataVO shoppingDataVO = ShoppingDataVO.builder().build();
            BeanUtils.copyBeanProp(shoppingDataVO, ttOrnaments);
            shoppingDataVO.setUseCredits(ObjectUtil.isEmpty(ttOrnaments.getUsePrice()) ? BigDecimal.ZERO : ttOrnaments.getUsePrice().multiply(new BigDecimal(exchangePriceRatio)));
            return shoppingDataVO;
        }).collect(Collectors.toList());
        PageDataInfo<ShoppingDataVO> pageDataInfo = new PageDataInfo<>();
        pageDataInfo.setCode(HttpStatus.SUCCESS);
        pageDataInfo.setMsg("查询成功");
        pageDataInfo.setRows(resultList);
        pageDataInfo.setTotal(new PageInfo<>(list).getTotal());
        return pageDataInfo;
    }
}
