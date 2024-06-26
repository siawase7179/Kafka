package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
public class Config {
    
    @Bean
    @ConfigurationProperties(prefix = "spring.kafka")
    public KafkaConfig getKafka(){
        return new KafkaConfig();
    }

	@Getter @Setter
    public static class KafkaConfig{
        private String bootstrapServers;
        private String topics;
        private Sasl sasl = new Sasl();

        @Getter @Setter
        public static class Sasl {
            private boolean enabled = false;
            private String jaasConfig;
        }
    }
}
