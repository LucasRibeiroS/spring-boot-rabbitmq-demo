package com.example.rabbitmqdemo.repositories;

import com.example.rabbitmqdemo.entities.Message;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {    
}