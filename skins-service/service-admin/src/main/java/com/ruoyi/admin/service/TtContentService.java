package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtContent;

import java.util.List;

public interface TtContentService extends IService<TtContent> {

    List<TtContent> queryList(TtContent ttContent);
}
