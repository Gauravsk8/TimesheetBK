package com.example.timesheet.service;

import com.example.timesheet.dto.request.EmployeeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaConsumerService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.name}")
    private String topic;

    @Value("${kafka.topic.Json.name}")
    private String topicJson;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public List<String> fetchMessages() {
        // String randomGroupId = "temp-group-" + System.currentTimeMillis();
        // Set Kafka consumer properties
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from the earliest available message
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Don't commit offsets


        // Create Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        //consumer.subscribe(Collections.singletonList(topic));

        // Manually assign and seek to beginning of the topic
        TopicPartition partition = new TopicPartition(topic, 0); // assuming single partition
        consumer.assign(List.of(partition));
        consumer.seekToBeginning(List.of(partition));
        System.out.println("consumer"+consumer);
        // Fetch messages from the topic
        List<String> messages = new ArrayList<>();
        boolean keepPolling = true;
        while (keepPolling) {
            var records = consumer.poll(Duration.ofMillis(1000)); // Polling for messages
            records.forEach(record -> {
                System.out.println("records value:" + record.value());
                messages.add(record.value());
            });

            if (records.isEmpty()) {
                keepPolling = false; // Stop polling if no more messages are available
            }
        }

        // Close the consumer
        consumer.close();
        return messages;
    }

    public List<EmployeeDto> fetchJsonMessages() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Configure JsonDeserializer for EmployeeDto
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.timesheet.dto.request.EmployeeDto");

        KafkaConsumer<String, EmployeeDto> consumer = new KafkaConsumer<>(properties);

        TopicPartition partition = new TopicPartition(topicJson, 0);
        consumer.assign(List.of(partition));
        consumer.seekToBeginning(List.of(partition));

        List<EmployeeDto> employeeMessages = new ArrayList<>();
        boolean keepPolling = true;

        while (keepPolling) {
            var records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(record -> {
                System.out.println("Employee Record: " + record.value());
                employeeMessages.add(record.value());
            });

            if (records.isEmpty()) {
                keepPolling = false;
            }
        }

        consumer.close();
        return employeeMessages;
    }



}
