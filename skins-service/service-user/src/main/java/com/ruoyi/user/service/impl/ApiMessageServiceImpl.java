package com.ruoyi.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.domain.other.TtMessageSend;
import com.ruoyi.admin.service.TtMessageSendService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.user.mapper.ApiMessageMapper;
import com.ruoyi.user.service.ApiMessageService;
import com.ruoyi.domain.vo.ApiMessageDataVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiMessageServiceImpl implements ApiMessageService {

    private final ApiMessageMapper apiMessageMapper;
    private final TtMessageSendService messageSendService;

    public ApiMessageServiceImpl(ApiMessageMapper apiMessageMapper,
                                 TtMessageSendService messageSendService) {
        this.apiMessageMapper = apiMessageMapper;
        this.messageSendService = messageSendService;
    }

    @Override
    public List<ApiMessageDataVO> getMessageList(Long userId, Integer id) {
        return apiMessageMapper.getMessageList(userId, id);
    }

    @Override
    public ApiMessageDataVO view(Long userId, Integer id) {
        TtMessageSend ttMessageSend = messageSendService.getById(id);
        ttMessageSend.setStatus("1");
        ttMessageSend.setReadingTime(DateUtils.getNowDate());
        messageSendService.updateById(ttMessageSend);
        List<ApiMessageDataVO> messageList = apiMessageMapper.getMessageList(userId, id);
        if (messageList.isEmpty()) return null;
        return messageList.get(0);
    }

    @Override
    public String batchOperation(Long userId, List<Integer> ids, String status) {
        List<TtMessageSend> list = new LambdaQueryChainWrapper<>(messageSendService.getBaseMapper())
                .eq(TtMessageSend::getRecId, userId)
                .in(TtMessageSend::getId, ids)
                .list();
        list = list.stream().peek(ttMessageSend -> {
            ttMessageSend.setStatus(status);
            if (StringUtils.isNull(ttMessageSend.getReadingTime())) {
                ttMessageSend.setReadingTime(DateUtils.getNowDate());
            }
        }).collect(Collectors.toList());
        messageSendService.updateBatchById(list, 1);
        return "";
    }
}
