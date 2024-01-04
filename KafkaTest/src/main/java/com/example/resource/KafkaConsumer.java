package com.example.resource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Profile(value = "consumer")
@Component
public class KafkaConsumer  implements AcknowledgingMessageListener<String, String> {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        // TODO Auto-generated method stub
        LOGGER.info("(deque) {}", data.value());
       
    }
} 