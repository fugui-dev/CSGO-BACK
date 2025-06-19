package com.ruoyi.admin.service;

import com.ruoyi.domain.vo.WebsitePropertyDataVO;

import java.util.List;

public interface WebsitePropertyService {

    List<WebsitePropertyDataVO> list();

    WebsitePropertyDataVO getById(Integer id);

    String save(WebsitePropertyDataVO websitePropertyDataVO);

    String updateWebsitePropertyById(WebsitePropertyDataVO websitePropertyDataVO);

    int deleteWebsitePropertyByIds(Integer[] ids);
}
