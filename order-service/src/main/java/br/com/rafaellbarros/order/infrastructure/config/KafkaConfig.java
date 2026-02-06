package br.com.rafaellbarros.order.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("!test")
@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Value("${app.kafka.partitions}")
    private int partitions;

    @Value("${app.kafka.replication-factor}")
    private short replication;

    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replication)
                .build();
    }
}