package br.com.rafaellbarros.order.infrastructure.persistence.eventstore;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("order_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDocument {

    @Id
    private String id;

    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private Integer version;
    private Instant timestamp;

    private Object payload;
}
