package com.example.timesheet.service;

import com.example.timesheet.Notification.NotificationService;
import com.example.timesheet.dto.request.EmployeeDto;
import com.example.timesheet.dto.request.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@AllArgsConstructor
public class KafkaListeners {
//
//    @Value("${kafka.topic.name}")
//    private String topic;
//    @Value("${spring.kafka.consumer.group-id}")
//    private String groupId;

    private final NotificationService notificationService;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<EmployeeDto> messageJsonQueue = new LinkedBlockingQueue<EmployeeDto>();

    private final BlockingQueue<String> timeSheetRemainderEvents = new LinkedBlockingQueue<String>();
    @KafkaListener(topics ="${kafka.topic.name}" ,groupId ="${spring.kafka.consumer.group-id}")
    public void listen(String message){
        System.out.println("Message listened:"+message);
        messageQueue.offer(message);  // Add the message to the queue
    }


    @KafkaListener(topics ="${kafka.topic.Json.name}" ,groupId ="${spring.kafka.consumer.group-id}")
    public void listenJson(EmployeeDto employeeDto){
        messageJsonQueue.offer(employeeDto);
    }
    public List<String> getMessage(){
        return List.copyOf(messageQueue);
    }

    @KafkaListener(topics ="${kafka.topic.timesheet.remainder}" ,groupId ="${spring.kafka.consumer.group-id}")
    public void listenRemainder(NotificationRequest event){
        System.out.println("Remainder listened:"+event);
         // Add the message to the queue
        event.setChannels(List.of("EMAIL"));
        notificationService.dispatch(event);
    }
}
