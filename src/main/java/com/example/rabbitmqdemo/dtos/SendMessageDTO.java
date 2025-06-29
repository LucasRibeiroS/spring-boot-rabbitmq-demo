package com.example.rabbitmqdemo.dtos;

public class SendMessageDTO {
    private String text;

    public SendMessageDTO() {
    }

    public SendMessageDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
