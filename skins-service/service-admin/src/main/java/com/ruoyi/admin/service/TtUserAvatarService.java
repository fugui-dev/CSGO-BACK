package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtUserAvatar;

public interface TtUserAvatarService extends IService<TtUserAvatar> {

    String updateUserAvatarById(TtUserAvatar ttUserAvatar);
}
