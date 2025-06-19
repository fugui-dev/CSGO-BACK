package com.ruoyi.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.domain.other.TtMessage;
import com.ruoyi.domain.other.TtMessageSend;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.admin.mapper.TtMessageMapper;
import com.ruoyi.admin.service.TtMessageSendService;
import com.ruoyi.admin.service.TtMessageService;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TtMessageServiceImpl extends ServiceImpl<TtMessageMapper, TtMessage> implements TtMessageService {

    private final TtUserService userService;
    private final TtMessageSendService messageSendService;

    public TtMessageServiceImpl(TtUserService userService,
                                TtMessageSendService messageSendService) {
        this.userService = userService;
        this.messageSendService = messageSendService;
    }

    @Override
    public List<TtMessage> queryList(TtMessage ttMessage) {
        LambdaQueryWrapper<TtMessage> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotNull(ttMessage.getId())) wrapper.eq(TtMessage::getId, ttMessage.getId());
        if (StringUtils.isNotEmpty(ttMessage.getMessage())) wrapper.like(TtMessage::getMessage, ttMessage.getMessage());
        return this.list(wrapper);
    }

    @Override
    public String delByIds(List<Integer> ids) {
        this.removeByIds(ids);
        return "";
    }

    @Override
    public String singleMessage(Integer messageId, List<Integer> userIds) {
        List<TtMessageSend> messageSends = new ArrayList<>();
        for (Integer userId : userIds) {
            TtMessageSend messageSend = TtMessageSend.builder().build();
            messageSend.setRecId(userId);
            messageSend.setMessageId(messageId);
            messageSend.setSendTime(DateUtils.getNowDate());
            messageSends.add(messageSend);
        }
        messageSendService.saveBatch(messageSends, 1);
        return "";
    }

    @Override
    public String massMessaging(Integer messageId) {
        List<TtUser> list = userService.list();
        List<TtMessageSend> messageSends = new ArrayList<>();
        for (TtUser ttUser : list) {
            TtMessageSend messageSend = TtMessageSend.builder().build();
            messageSend.setRecId(ttUser.getUserId());
            messageSend.setMessageId(messageId);
            messageSend.setSendTime(DateUtils.getNowDate());
            messageSends.add(messageSend);
        }
        messageSendService.saveBatch(messageSends, 1);
        return "";
    }
}
