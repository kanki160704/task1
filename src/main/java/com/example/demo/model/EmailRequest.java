package com.example.demo.model;

import lombok.Data;

@Data
public class EmailRequest {
    private String recipient;
    private String startTime;
    private String endTime;
    private String subject;
    private String description;
    private String templateId;
}
