package com.example.resource;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Profile(value = "consumer")
@Component
@Log4j2
public class KafkaConsumer  implements AcknowledgingMessageListener<String, String> {
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        // TODO Auto-generated method stub
        try{
            if(data.value().equals("q")){
                throw new Exception();
            }
            log.info(data.value());
            acknowledgment.acknowledge();
        }catch(Exception e){
            log.error("error", e);
            throw new RuntimeException(e);
        }
    }
} 