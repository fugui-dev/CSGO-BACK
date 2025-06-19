package com.ruoyi.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.domain.other.TtBoxType;

public interface TtBoxTypeService extends IService<TtBoxType> {

    String updateBoxTypeById(TtBoxType ttBoxType);
}
