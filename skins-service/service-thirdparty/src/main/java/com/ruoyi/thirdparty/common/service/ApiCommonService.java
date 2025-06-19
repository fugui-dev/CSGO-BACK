package com.ruoyi.thirdparty.common.service;

import com.ruoyi.common.core.domain.R;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ApiCommonService {

    R<Object> writerPayImage(HttpServletResponse response, String contents) throws IOException;
}
