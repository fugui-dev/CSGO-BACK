package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtContentType;

import java.util.List;

public interface TtContentTypeService extends IService<TtContentType> {
    List<TtContentType> queryList(TtContentType ttContentType);
}
