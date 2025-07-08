package com.example.rabbitmqdemo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue config
    
    // This queue is consumed manually by the application
    public static final String EXCHANGE_NAME = "demo.internal.exchange";
    public static final String QUEUE_NAME = "demo.internal.queue"; 
    public static final String ROUTING_KEY = "demo.internal.routingkey";
    
    // New queue for consuming messages from external applications
    public static final String EXTERNAL_EXCHANGE_NAME = "demo.external.exchange";
    public static final String EXTERNAL_QUEUE_NAME = "demo.external.queue";
    public static final String EXTERNAL_ROUTING_KEY = "demo.external.routingkey";
    
    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue queue() {
        // durable=true -> a fila sobrevive a um reinicio do broker RabbitMQ
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    TopicExchange externalExchange() {
        return new TopicExchange(EXTERNAL_EXCHANGE_NAME);
    }

    @Bean
    Queue externalQueue() {
        return new Queue(EXTERNAL_QUEUE_NAME, true);
    }

    @Bean
    Binding externalBinding(Queue externalQueue, TopicExchange externalExchange) {
        return BindingBuilder.bind(externalQueue).to(externalExchange).with(EXTERNAL_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}