package com.ruoyi.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.admin.mapper.TtOrnamentMapper;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.domain.entity.TtOrnament;
import com.ruoyi.admin.service.WebsitePropertyService;
import com.ruoyi.domain.vo.WebsitePropertyDataVO;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WebsitePropertyServiceImpl implements WebsitePropertyService {

    private final TtOrnamentMapper ornamentsMapper;

    public WebsitePropertyServiceImpl(TtOrnamentMapper ornamentsMapper) {
        this.ornamentsMapper = ornamentsMapper;
    }

    @Override
    public List<WebsitePropertyDataVO> list() {
        return ornamentsMapper.list();
    }

    @Override
    public WebsitePropertyDataVO getById(Integer id) {
        TtOrnament ornaments = new LambdaQueryChainWrapper<>(ornamentsMapper)
                .eq(TtOrnament::getId, id)
                .eq(TtOrnament::getIsProprietaryProperty, "0")
                .one();
        WebsitePropertyDataVO websitePropertyDataVO = WebsitePropertyDataVO.builder().build();
        BeanUtils.copyBeanProp(websitePropertyDataVO, ornaments);
        return websitePropertyDataVO;
    }

    @Override
    public String save(WebsitePropertyDataVO websitePropertyDataVO) {
        if (StringUtils.isEmpty(websitePropertyDataVO.getImageUrl())) websitePropertyDataVO.setImageUrl("");
        else websitePropertyDataVO.setImageUrl(RuoYiConfig.getDomainName() + websitePropertyDataVO.getImageUrl());
        websitePropertyDataVO.setCreateTime(DateUtils.getNowDate());
        websitePropertyDataVO.setIsPutaway("1");
        websitePropertyDataVO.setIsProprietaryProperty("0");
        TtOrnament ttOrnament = TtOrnament.builder().build();
        BeanUtils.copyBeanProp(ttOrnament, websitePropertyDataVO);
        ttOrnament.setMarketHashName(UUID.randomUUID().toString());
        ttOrnament.setId(IdUtil.getSnowflake().nextId() % 1000000000L);
        ttOrnament.setShortName(websitePropertyDataVO.getName());
        ornamentsMapper.insert(ttOrnament);
        return "";
    }

    @Override
    public String updateWebsitePropertyById(WebsitePropertyDataVO websitePropertyDataVO) {
        websitePropertyDataVO.setUpdateTime(DateUtils.getNowDate());
        String imageUrl = websitePropertyDataVO.getImageUrl();
        websitePropertyDataVO.setImageUrl(RuoYiConfig.getDomainName() + imageUrl);
        // TtOrnament ttOrnament = TtOrnament.builder().build();
        // BeanUtils.copyBeanProp(ttOrnament, websitePropertyDataVO);
        // ornamentsMapper.updateById(ttOrnament);
        ornamentsMapper.updateWebsiteProperty(websitePropertyDataVO);
        return "";
    }

    @Override
    public int deleteWebsitePropertyByIds(Integer[] ids) {
        return ornamentsMapper.deleteWebsitePropertyByIds(ids);
    }
}
