package com.example.timesheet.service;

import com.example.timesheet.dto.request.EmployeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Value("${kafka.topic.name}")
    private String topic;

    @Value("${kafka.topic.Json.name}")
    private String topicJson;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private  KafkaTemplate<String,EmployeeDto> kafkaTemplateJson;
    public void sendMessage(String message){
        kafkaTemplate.send(topic,message);
        System.out.println("Message:"+message);
    }

    public void sendJsonMessage(EmployeeDto employeeDto){
        Message<EmployeeDto> message= MessageBuilder
                .withPayload(employeeDto)
                .setHeader(KafkaHeaders.TOPIC,topicJson)
                .build();
        kafkaTemplateJson.send(message);


    }


}
