package br.com.rafaellbarros.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "br.com.rafaellbarros.order.infrastructure.persistence")
public class MongoConfig {
}
