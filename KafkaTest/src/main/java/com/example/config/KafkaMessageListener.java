package com.example.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.resource.KafkaConsumer;
import com.example.resource.KafkaDeadLetterPublisher;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;


@Configuration
@RequiredArgsConstructor
public class KafkaMessageListener {
	@Value("${spring.kafka.topics}")
	private String topics;

    private final Config config;

	private Map<String, Object> kafkaProperties(){
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		
		if (config.getKafka().getSasl().isEnabled()){
			props.put("security.protocol", "SASL_SSL");
			props.put("sasl.mechanism", "SCRAM-SHA-512");
			props.put("sasl.jaas.config", config.getKafka().getSasl().getJaasConfig());
		}
		return props;
	}

	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getBootstrapServers());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-test-group");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		if (config.getKafka().getSasl().isEnabled()){
			props.put("security.protocol", "SASL_SSL");
			props.put("sasl.mechanism", "SCRAM-SHA-512");
			props.put("sasl.jaas.config", config.getKafka().getSasl().getJaasConfig());
		}
		
		return new DefaultKafkaConsumerFactory<>(props);
	}


	@Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = kafkaProperties();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory){
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	public DefaultErrorHandler defaultErrorHandler(KafkaDeadLetterPublisher kafkaDeadLetterPublisher) {
		return new DefaultErrorHandler(kafkaDeadLetterPublisher.deadLetterPublishingRecoverer(), new FixedBackOff(0L, 1));
	}
	

	@Bean
	public ConcurrentMessageListenerContainer<String, String> ConcurrentMessageListenerContainer(KafkaConsumer kafkaConsumer, DefaultErrorHandler defaultErrorHandler){
		
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setConcurrency(3);
		factory.setCommonErrorHandler(defaultErrorHandler);
		factory.getContainerProperties().setAckMode(AckMode.MANUAL);

		ConcurrentMessageListenerContainer<String, String> concurrentMessageListenerContainer = factory.createContainer(topics.split(","));
		concurrentMessageListenerContainer.setupMessageListener(kafkaConsumer);
		concurrentMessageListenerContainer.setAutoStartup(true);

		return concurrentMessageListenerContainer;
	}

	@Bean
    public KafkaAdmin kafkaAdmin(){
		KafkaAdmin kafkaAdmin = new KafkaAdmin(kafkaProperties());
		Map<String, TopicDescription> topcisMap = kafkaAdmin.describeTopics(topics.split(","));
		
		for (String topic : topcisMap.keySet()){
			TopicDescription topicDescription = topcisMap.get(topic);
			Iterator<TopicPartitionInfo> itPartition = topicDescription.partitions().iterator();
			while(itPartition.hasNext()){
				TopicPartitionInfo topicPartitionInfo = itPartition.next();
				System.out.println("topic:"+ topic);
				System.out.println("partition:"+topicPartitionInfo.partition());
				System.out.println("leader node:"+ topicPartitionInfo.leader().toString());
				System.out.println("in-sync replicas of the partition:"+topicPartitionInfo.isr());
				System.out.println("replicas of the partition:" + topicPartitionInfo.replicas());
				System.out.println();
			}
		};
		
        return kafkaAdmin;
    }
}
