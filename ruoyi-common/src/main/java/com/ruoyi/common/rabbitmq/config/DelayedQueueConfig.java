package com.ruoyi.common.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedQueueConfig {

    //死信交换机，队列，路由相关配置
    public static final String DLK_EXCHANGE = "dlk_exchange";
    public static final String DLK_ROUTEKEY = "dlk_routeKey";
    public static final String DLK_QUEUE = "dlk_queue";

    // 业务交换机...
    public static final String OPEN_ROLL_EXCHANGE = "open_roll_exchange";
    public static final String OPEN_ROLL_QUEUE = "open_roll_queue";
    public static final String OPEN_ROLL_KEY = "open_roll_key";

    // 业务交换机...
    public static final String DELIVERY_QUEUE = "delivery_queue";


    // roll开奖倒计时
    @Bean
    public CustomExchange openRollExchange() {
        return new CustomExchange(OPEN_ROLL_EXCHANGE,"direct", true, false);
    }
    @Bean
    public Queue openRollQueue() {

        //只需要在声明业务队列时添加x-dead-letter-exchange，值为死信交换机
        Map<String,Object> map = new HashMap<>(1);
        map.put("x-dead-letter-exchange",DLK_EXCHANGE);
        //该参数x-dead-letter-routing-key可以修改该死信的路由key，不设置则使用原消息的路由key
        map.put("x-dead-letter-routing-key",DLK_ROUTEKEY);

        return new Queue(OPEN_ROLL_QUEUE,true,false,false,map);
    }
    @Bean
    public Binding openRollBind(Queue openRollQueue, CustomExchange openRollExchange) {
        return BindingBuilder
                .bind(openRollQueue)
                .to(openRollExchange)
                .with(OPEN_ROLL_KEY)
                .noargs();
    }

    // 死信交换机
    @Bean
    public DirectExchange dlkExchange(){
        return new DirectExchange(DLK_EXCHANGE,true,false);
    }
    @Bean
    public Queue dlkQueue(){
        return new Queue(DLK_QUEUE,true,false,false);
    }
    @Bean
    public Binding dlkBind(){
        return BindingBuilder.bind(dlkQueue()).to(dlkExchange()).with(DLK_ROUTEKEY);
    }



    // 自动提货业务消息队列
    //@Bean
    public Queue deliveryQueue() {
        return new Queue(DELIVERY_QUEUE);
    }


}

