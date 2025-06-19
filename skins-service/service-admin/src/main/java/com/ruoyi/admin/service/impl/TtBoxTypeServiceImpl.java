package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtBoxType;
import com.ruoyi.admin.mapper.TtBoxTypeMapper;
import com.ruoyi.admin.service.TtBoxTypeService;
import com.ruoyi.common.config.RuoYiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TtBoxTypeServiceImpl extends ServiceImpl<TtBoxTypeMapper, TtBoxType> implements TtBoxTypeService {

    @Override
    public String updateBoxTypeById(TtBoxType ttBoxType) {
        String icon = ttBoxType.getIcon();
        ttBoxType.setIcon(RuoYiConfig.getDomainName() + icon);
        this.updateById(ttBoxType);
        return "";
    }
}
