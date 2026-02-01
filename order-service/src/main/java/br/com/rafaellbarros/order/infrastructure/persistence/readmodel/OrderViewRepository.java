package br.com.rafaellbarros.order.infrastructure.persistence.readmodel;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface OrderViewRepository extends ReactiveMongoRepository<OrderView, String> {

    Flux<OrderView> findByUserId(String userId);
}