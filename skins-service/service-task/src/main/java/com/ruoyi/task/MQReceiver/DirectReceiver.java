package com.ruoyi.task.MQReceiver;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.ruoyi.admin.service.TtUserService;
import com.ruoyi.domain.entity.sys.TtUser;
import com.ruoyi.domain.common.constant.TtAccountRecordSource;
import com.ruoyi.domain.common.constant.TtAccountRecordType;
import com.ruoyi.domain.task.DTO.pWelfareMQData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;

import static com.ruoyi.task.config.DirectExchangeConfig.*;


// 不用
@Slf4j
// @Component
public class DirectReceiver {

}
