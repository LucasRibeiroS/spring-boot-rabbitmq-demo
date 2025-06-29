package com.example.rabbitmqdemo.services;

import com.example.rabbitmqdemo.config.RabbitMQConfig;
import com.example.rabbitmqdemo.constants.MessageStatus;
import com.example.rabbitmqdemo.dtos.MessageDTO;
import com.example.rabbitmqdemo.dtos.SendMessageDTO;
import com.example.rabbitmqdemo.entities.Message;
import com.example.rabbitmqdemo.repositories.MessageRepository;
import com.example.rabbitmqdemo.services.exceptions.ResourceNotFound;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Busca uma mensagem pelo ID no banco de dados.
     * @param id O ID da mensagem a ser buscada.
     * @return O DTO da mensagem encontrada.
     * @throws ResourceNotFound Se a mensagem não for encontrada.
     */
    public MessageDTO getMessageById(String id) throws ResourceNotFound {
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Message not found"));
        return convertEntityToDto(message);
    }

    /*  
     * Consome automaticamente mensagens de uma fila externa.
     */
    @RabbitListener(queues = RabbitMQConfig.EXTERNAL_QUEUE_NAME)
    public void autoConsume(@Payload String messageText, @Header("amqp_messageId") String messageId) {
        System.out.println("Mensagem consumida automaticamente: " + messageText);
        System.out.println("Message ID: " + messageId);

        Message message = new Message();
        message.setId(messageId);
        message.setText(messageText);
        message.setStatus(MessageStatus.CONSUMED);
        message = messageRepository.save(message);
    }

    /**
     * Lê a próxima mensagem da fila de forma programática.
     * Este método consome a mensagem (ela é removida da fila).
     * @return O DTO da mensagem lida
     * @throws ResourceNotFound Se a fila estiver vazia.
     */
    public MessageDTO consume() throws ResourceNotFound {
        // Recebe a mensagem completa (com propriedades)
        org.springframework.amqp.core.Message rabbitMessage = rabbitTemplate.receive(RabbitMQConfig.QUEUE_NAME);
        if (rabbitMessage == null) {
            throw new ResourceNotFound("Queue is empty");
        }

        System.out.println("Mensagem lida sob demanda: " + rabbitMessage.getMessageProperties());

        Message message = new Message();
        message.setId(rabbitMessage.getMessageProperties().getMessageId());
        message.setText((String) rabbitTemplate.getMessageConverter().fromMessage(rabbitMessage));
        message.setStatus(MessageStatus.CONSUMED);
        message = messageRepository.save(message);

        // Converte o corpo da mensagem para MessageDTO
        return convertEntityToDto(message);
    }

    /**
     * Envia uma mensagem para a fila do RabbitMQ.
     * @param sendMessageDTO O objeto de transferência de dados contendo a mensagem.
     * @return O ID da mensagem gerado pelo RabbitMQ.
     */
    public MessageDTO sendMessage(SendMessageDTO sendMessageDTO) {
        System.out.println("Enviando mensagem para o RabbitMQ: " + sendMessageDTO.toString());

        String messageId = UUID.randomUUID().toString();

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            sendMessageDTO.getText(),
            message -> {
                message.getMessageProperties().setMessageId(messageId);
                return message;
            }
        );

        Message message = messageRepository.save(
            new Message(
                messageId,
                sendMessageDTO.getText(),
                MessageStatus.SENT,
                Instant.now(),
                Instant.now()
            )
        );
        
        return convertEntityToDto(message);
    }

    /**
     * Limpa (apaga) todas as mensagens de uma fila.
     * @return A quantidade de mensagens que foram apagadas.
     */
    public void purgeQueue() {
        System.out.println("Limpando a fila: " + RabbitMQConfig.QUEUE_NAME);
        rabbitAdmin.purgeQueue(RabbitMQConfig.QUEUE_NAME);
    }

    /*
     * Converte uma entidade Message para um DTO MessageDTO.
     */
    private MessageDTO convertEntityToDto(Message message) {
        return new MessageDTO(
            message.getId(),
            message.getText(),
            message.getStatus(),
            message.getCreatedAt(),
            message.getUpdatedAt()
        );
    }
}