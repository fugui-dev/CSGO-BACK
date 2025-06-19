package com.ruoyi.playingmethod.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.admin.mapper.TtFightUserMapper;
import com.ruoyi.domain.other.TtFightUser;
import com.ruoyi.playingmethod.service.ApiFightUserService;
import org.springframework.stereotype.Service;

@Service
public class ApiFightUserServiceImpl extends ServiceImpl<TtFightUserMapper, TtFightUser> implements ApiFightUserService {
}
