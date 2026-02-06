package br.com.rafaellbarros.order.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Profile("!test")
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        listenerLogs(kafkaTemplate);
        return kafkaTemplate;
    }

    private static void listenerLogs(KafkaTemplate<String, Object> kafkaTemplate) {
        kafkaTemplate.setProducerListener(new ProducerListener<>() {
            @Override
            public void onSuccess(ProducerRecord<String, Object> record, RecordMetadata metadata) {
                log.info("Log Sucesso: Mensagem enviada para o tópico {} [partição: {}] com offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
            @Override
            public void onError(ProducerRecord<String, Object> record,
                                RecordMetadata metadata, Exception exception) {
                log.error("Log Erro: Falha ao enviar para o tópico {}. Erro: {}",
                        record.topic(), exception.getMessage());
            }
        });
    }

}