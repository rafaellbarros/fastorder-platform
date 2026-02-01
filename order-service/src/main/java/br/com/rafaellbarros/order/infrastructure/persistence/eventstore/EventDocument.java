package br.com.rafaellbarros.order.infrastructure.persistence.eventstore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("event_store")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "aggregate_version_idx", def = "{'aggregateId':1,'version':1}", unique = true),
        @CompoundIndex(name = "event_id_idx", def = "{'eventId':1}", unique = true)
})
public class EventDocument {

    @Id
    private String id;

    private String eventId;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private Long version;
    private Instant timestamp;

    private String payload;
}
