package com.example.resource;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class KafkaDeadLetterPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (record, exception) -> {
            String dltTopic = record.topic() + "-DLT";
            log.info("Sending message to DLT topic: " + dltTopic);
            return new TopicPartition(dltTopic, record.partition());
        });
    }
}
