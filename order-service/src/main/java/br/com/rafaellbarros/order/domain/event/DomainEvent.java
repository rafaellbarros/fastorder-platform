package br.com.rafaellbarros.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class DomainEvent {

    private String eventId;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private Integer version;
    private Instant timestamp;
}
