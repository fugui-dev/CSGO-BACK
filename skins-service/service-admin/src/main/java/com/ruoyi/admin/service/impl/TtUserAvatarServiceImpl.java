package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtUserAvatar;
import com.ruoyi.admin.mapper.TtUserAvatarMapper;
import com.ruoyi.admin.service.TtUserAvatarService;
import com.ruoyi.common.config.RuoYiConfig;
import org.springframework.stereotype.Service;

@Service
public class TtUserAvatarServiceImpl extends ServiceImpl<TtUserAvatarMapper, TtUserAvatar> implements TtUserAvatarService {

    @Override
    public String updateUserAvatarById(TtUserAvatar ttUserAvatar) {
        String avatar = ttUserAvatar.getAvatar();
        ttUserAvatar.setAvatar(RuoYiConfig.getDomainName() + avatar);
        this.updateById(ttUserAvatar);
        return "";
    }
}
