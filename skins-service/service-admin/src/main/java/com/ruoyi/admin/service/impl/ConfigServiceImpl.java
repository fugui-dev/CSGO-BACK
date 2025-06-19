package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.ConfigData;
import com.ruoyi.admin.mapper.ConfigMapper;
import com.ruoyi.admin.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigData> implements ConfigService {
}
