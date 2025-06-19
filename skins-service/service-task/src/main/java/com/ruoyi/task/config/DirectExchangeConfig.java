package com.ruoyi.task.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;


@Configurable
public class DirectExchangeConfig {

    @Autowired
    private AmqpAdmin amqpAdmin;
    public static final String PROMOTION_WELFARE_QUEUE = "pWelfareQueue";
    public static final String PROMOTION_WELFARE_EXCHANGE = "pWelfareExchange";
    public static final String PROMOTION_WELFARE_KEY1 = "pwKey1";

    // public static void main(String[] args) {
    //     System.out.println(Runtime.getRuntime().maxMemory() / 1024.0 / 1024 + "M");
    // }


    // @Bean
    // public Queue directQueue() {
    //     return new Queue(DIRECT_QUEUE, true);
    // }
    //
    // @Bean
    // public DirectExchange directExchange() {
    //     return new DirectExchange(DIRECT_EXCHANGE, true, false);
    // }
    //
    // @Bean
    // public Binding bindingDirectExchange(Queue directQueue, DirectExchange directExchange) {
    //     return BindingBuilder.bind(directQueue).to(directExchange).with(DIRECT_ROUTING_KEY);
    // }

    // @Bean
    // public Queue directQueue2() {
    //     return new Queue(DIRECT_QUEUE2, true);
    // }

    // @Bean
    // public Binding bindingDirectExchange2(Queue directQueue2, DirectExchange directExchange) {
    //     return BindingBuilder.bind(directQueue2).to(directExchange).with(DIRECT_ROUTING_KEY);
    // }


}
