package com.ruoyi.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.domain.other.TtBanner;
import com.ruoyi.domain.other.TtContent;
import com.ruoyi.domain.other.TtContentType;
import com.ruoyi.admin.mapper.TtBannerMapper;
import com.ruoyi.admin.mapper.TtContentMapper;
import com.ruoyi.admin.mapper.TtContentTypeMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.user.service.ApiWebsiteSetupService;
import com.ruoyi.domain.vo.ApiContentDataVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiWebsiteSetupServiceImpl implements ApiWebsiteSetupService {

    private final TtBannerMapper bannerMapper;
    private final TtContentTypeMapper contentTypeMapper;
    private final TtContentMapper contentMapper;

    public ApiWebsiteSetupServiceImpl(TtBannerMapper bannerMapper,
                                      TtContentTypeMapper contentTypeMapper,
                                      TtContentMapper contentMapper) {
        this.bannerMapper = bannerMapper;
        this.contentTypeMapper = contentTypeMapper;
        this.contentMapper = contentMapper;
    }

    @Override
    public List<TtBanner> getBannerList() {
        return new LambdaQueryChainWrapper<>(bannerMapper)
                .eq(TtBanner::getStatus, "0")
                .orderByAsc(TtBanner::getSort)
                .list();
    }

    @Override
    public ApiContentDataVO getContentByType(String alias) {
        ApiContentDataVO result = ApiContentDataVO.builder().build();
        TtContentType contentType = new LambdaQueryChainWrapper<>(contentTypeMapper).eq(TtContentType::getAlias, alias)
                .eq(TtContentType::getStatus, "0").one();
        if (StringUtils.isNull(contentType)) return result;
        result.setContentTypeId(contentType.getId());
        result.setContentTypeName(contentType.getName());
        result.setContentAlias(contentType.getAlias());
        List<TtContent> list = new LambdaQueryChainWrapper<>(contentMapper).eq(TtContent::getTypeId, contentType.getId())
                .eq(TtContent::getStatus, "0").list();
        result.setContentList(list);
        return result;
    }
}
