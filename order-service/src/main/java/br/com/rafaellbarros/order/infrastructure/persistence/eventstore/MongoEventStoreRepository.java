package br.com.rafaellbarros.order.infrastructure.persistence.eventstore;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class MongoEventStoreRepository implements EventStoreRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Void> save(Object event) {

        DomainEvent domainEvent = (DomainEvent) event;

        EventDocument document = EventDocument.builder()
                .id(domainEvent.getEventId())
                .aggregateId(domainEvent.getAggregateId())
                .aggregateType(domainEvent.getAggregateType())
                .eventType(domainEvent.getEventType())
                .version(domainEvent.getVersion())
                .timestamp(domainEvent.getTimestamp())
                .payload(domainEvent)
                .build();

        return mongoTemplate.save(document).then();
    }

    @Override
    public Flux<Object> findByAggregateId(String aggregateId) {
        return mongoTemplate.find(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("aggregateId").is(aggregateId)
                ),
                EventDocument.class
        ).map(EventDocument::getPayload);
    }
}