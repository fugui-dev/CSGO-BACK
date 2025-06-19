package com.ruoyi.common.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

// 弃用
//@Configuration
public class FightQueueConfig {

    // public static final String FIGHT_EXCHANGE = "fight_exchange";
    // public static final String FIGHT_QUEUE = "fight_queue";
    // public static final String FIGHT_ROUTING_KEY = "fight_routingKey";
    //
    // @Bean
    // public Queue fightQueue() {
    //     return new Queue(FIGHT_QUEUE);
    // }
    //
    // @Bean
    // public CustomExchange fightExchange() {
    //     HashMap<String, Object> arguments = new HashMap<>();
    //     arguments.put("x-fight-type", "direct");
    //     // arguments.put("x-fight-type", "direct");
    //     return new CustomExchange(FIGHT_EXCHANGE, "x-delayed-message", true, false, arguments);
    //     // return new CustomExchange(FIGHT_EXCHANGE, "x-fight-message", true, false, arguments);
    // }
    //
    //
    //
    // @Bean
    // public Binding fightQueueBindingFightExchange(Queue fightQueue, CustomExchange fightExchange) {
    //     return BindingBuilder.bind(fightQueue).to(fightExchange).with(FIGHT_ROUTING_KEY).noargs();
    // }
}

