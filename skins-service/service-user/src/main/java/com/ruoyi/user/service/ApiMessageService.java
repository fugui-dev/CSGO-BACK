package com.ruoyi.user.service;

import com.ruoyi.domain.vo.ApiMessageDataVO;

import java.util.List;

public interface ApiMessageService {

    List<ApiMessageDataVO> getMessageList(Long userId, Integer id);

    ApiMessageDataVO view(Long userId, Integer id);

    String batchOperation(Long userId, List<Integer> ids, String status);
}
