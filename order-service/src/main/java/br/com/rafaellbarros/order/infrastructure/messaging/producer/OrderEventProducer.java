package br.com.rafaellbarros.order.infrastructure.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publish(Object event) {
        return Mono.fromFuture(kafkaTemplate.send("order-events", event))
                .then();
    }
}
