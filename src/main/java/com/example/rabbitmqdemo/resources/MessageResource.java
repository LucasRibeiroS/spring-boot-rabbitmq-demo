package com.example.rabbitmqdemo.resources;

import com.example.rabbitmqdemo.dtos.MessageDTO;
import com.example.rabbitmqdemo.dtos.SendMessageDTO;
import com.example.rabbitmqdemo.services.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/messages")
public class MessageResource {

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<MessageDTO> getById(@PathVariable String id) {
        MessageDTO message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    @GetMapping(value = "/consume", produces = "application/json")
    public ResponseEntity<MessageDTO> consume() {
        MessageDTO message = messageService.consume();
        if (message != null) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.ok(message);
    }
    
    @PostMapping(consumes = "application/json")
    public ResponseEntity<MessageDTO> send(@RequestBody SendMessageDTO sendMessageDTO) {
        MessageDTO messageDTO = messageService.sendMessage(sendMessageDTO);
        return ResponseEntity.ok(messageDTO);
    }

    @DeleteMapping(value = "/clear", produces = "application/json")
    public ResponseEntity<Void> delete() {
        messageService.purgeQueue();
        return ResponseEntity.noContent().build();
    }
}