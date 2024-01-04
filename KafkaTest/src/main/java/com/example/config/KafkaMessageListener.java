package com.example.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.resource.KafkaConsumer;
import com.example.resource.KafkaErrorHandler;

import lombok.RequiredArgsConstructor;

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


@Configuration
@RequiredArgsConstructor
public class KafkaMessageListener {

	private final KafkaErrorHandler kafkaErorHandler;

	@Value("${spring.kafka.topics}")
	private String topics;

    @Autowired
    private Config config;

	private Map<String, Object> kafkaProperties(){
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return props;
	}

	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafka().getBootstrapServers());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-test-group");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Profile(value = "consumer")
	@Bean
	public ConcurrentMessageListenerContainer<String, String> ConcurrentMessageListenerContainer(KafkaConsumer kafkaConsumer){
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setConcurrency(10);
		
		ConcurrentMessageListenerContainer<String, String> concurrentMessageListenerContainer = factory.createContainer(topics.split(","));
		// ContainerProperties containerProperties = concurrentMessageListenerContainer.getContainerProperties();
		// containerProperties.setAckMode(AckMode.MANUAL_IMMEDIATE); // 수동 커밋 모드 설정
		
		concurrentMessageListenerContainer.setCommonErrorHandler(kafkaErorHandler);
		concurrentMessageListenerContainer.setupMessageListener(kafkaConsumer);
		concurrentMessageListenerContainer.setAutoStartup(true);
		

		return concurrentMessageListenerContainer;
	}

	
	@Profile(value = "producer")
	@Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = kafkaProperties();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

	@Profile(value = "producer")
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory){
		return new KafkaTemplate<>(producerFactory);
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
