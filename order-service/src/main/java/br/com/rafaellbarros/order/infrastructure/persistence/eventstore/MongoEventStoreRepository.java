package br.com.rafaellbarros.order.infrastructure.persistence.eventstore;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.exception.ConcurrencyException;
import br.com.rafaellbarros.order.domain.repository.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class MongoEventStoreRepository implements EventStoreRepository {

    private final ReactiveMongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> saveAll(String aggregateId, Long expectedVersion, List<DomainEvent> events) {

        return findLastVersion(aggregateId)
                .flatMapMany(lastVersion -> {

                    if (!lastVersion.equals(expectedVersion)) {
                        return Mono.error(new ConcurrencyException(
                                "Version conflict. Expected: " + expectedVersion + " but was: " + lastVersion));
                    }

                    AtomicLong nextVersion = new AtomicLong(lastVersion + 1);

                    return Flux.fromIterable(events)
                            .map(event -> EventDocument.builder()
                                    .id(UUID.randomUUID().toString())
                                    .eventId(event.getEventId())
                                    .aggregateId(aggregateId)
                                    .aggregateType(event.getAggregateType())
                                    .eventType(event.getEventType())
                                    .version(nextVersion.getAndIncrement())
                                    .timestamp(event.getOccurredAt())
                                    .payload(serialize(event))
                                    .build());
                })
                .concatMap(mongoTemplate::insert) // 1 a 1 → respeita ordem
                .onErrorMap(DuplicateKeyException.class,
                        e -> new ConcurrencyException("Duplicate event or version conflict"))
                .then();
    }

    private Mono<Long> findLastVersion(String aggregateId) {
        Query query = Query.query(Criteria.where("aggregateId").is(aggregateId))
                .with(Sort.by(Sort.Direction.DESC, "version"))
                .limit(1);

        return mongoTemplate.findOne(query, EventDocument.class)
                .map(EventDocument::getVersion)
                .defaultIfEmpty(0L);
    }

    @Override
    public Flux<DomainEvent> findByAggregateId(String aggregateId) {

        Query query = Query.query(Criteria.where("aggregateId").is(aggregateId))
                .with(Sort.by(Sort.Direction.ASC, "version"));

        return mongoTemplate.find(query, EventDocument.class)
                .map(this::deserialize);
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DomainEvent deserialize(EventDocument doc) {
        try {
            Class<?> clazz = Class.forName("br.com.rafaellbarros.order.domain.event." + doc.getEventType());
            return (DomainEvent) objectMapper.readValue(doc.getPayload(), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

