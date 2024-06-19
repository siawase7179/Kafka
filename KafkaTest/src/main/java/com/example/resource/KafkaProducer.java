package com.example.resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.config.Config;
import com.example.vo.TestDataVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile(value = "producer")
@Component
public class KafkaProducer implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private Config config;

    private ExecutorService workerPool;
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init(){
        workerPool = Executors.newCachedThreadPool();
        workerPool.execute(this);
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        LOGGER.info("Producer Started.");
        int i=0;
        while (true){
            if (i > Integer.MAX_VALUE){
                i=0;
            }
            String key = String.format("%010d", i++);
            TestDataVO testDataVO = new TestDataVO(key, System.currentTimeMillis()/1000, 0);
            try {
                kafkaTemplate.send(config.getKafka().getTopics(), mapper.writeValueAsString(testDataVO));
                LOGGER.info("(enque) {}", mapper.writeValueAsString(testDataVO));
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                LOGGER.error("error", e);
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                LOGGER.error("error", e);
            }
        }
    }

}
