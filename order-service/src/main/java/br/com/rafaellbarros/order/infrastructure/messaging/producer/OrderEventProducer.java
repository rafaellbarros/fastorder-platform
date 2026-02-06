package br.com.rafaellbarros.order.infrastructure.messaging.producer;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    public Flux<Void> publishAll(List<DomainEvent> events) {
        log.info("Publishing order events...");
        return Flux.fromIterable(events)
                .flatMap(event ->
                        Mono.fromFuture(kafkaTemplate.send(topicName, event.getAggregateId(), event))
                                .doOnSuccess(r -> log.info("Event {} sent", event.getEventType()))
                                .doOnError(e -> log.error("Kafka error", e))
                                .then()
                );
    }
}

