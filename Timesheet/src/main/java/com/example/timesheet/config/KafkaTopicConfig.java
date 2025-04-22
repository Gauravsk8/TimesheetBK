package com.example.timesheet.config;

import com.example.timesheet.dto.request.EmployeeDto;
import com.example.timesheet.dto.request.NotificationRequest;
import com.example.timesheet.dto.request.TimeSheetRemainderEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    // Define Kafka topics here
    @Bean
    public NewTopic topicExample() {
        return TopicBuilder.name("timesheet-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicJson() {
        return TopicBuilder.name("timesheet-topic-Json")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicRemainder() {
        return TopicBuilder.name("timesheet-remainder-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, EmployeeDto> employeeDtoProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, EmployeeDto> kafkaTemplateJson() {
        return new KafkaTemplate<>(employeeDtoProducerFactory());
    }


    // Kafka consumer factory
    @Bean
    public ConsumerFactory<String, TimeSheetRemainderEvent> reminderConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "timesheet-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(TimeSheetRemainderEvent.class).trustedPackages("*")
        );
    }

    // Kafka listener container factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TimeSheetRemainderEvent> reminderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TimeSheetRemainderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reminderConsumerFactory());
        return factory;
    }
    @Bean
    public ProducerFactory<String, NotificationRequest> notificationRequestProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, NotificationRequest> kafkaTemplateNotification() {
        return new KafkaTemplate<>(notificationRequestProducerFactory());
    }



}
