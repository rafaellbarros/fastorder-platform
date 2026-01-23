# FastOrder – API Gateway

## 📌 Visão Geral

O **FastOrder API Gateway** é um gateway reativo construído com **Spring Boot 3 + Spring Cloud Gateway (WebFlux)**, projetado para atuar como ponto único de entrada da plataforma FastOrder. Ele centraliza **roteamento**, **segurança**, **observabilidade** e **governança de APIs**, integrando-se com **Keycloak (OAuth2 / OpenID Connect)** e preparado para **Service Discovery**.

Este projeto foi estruturado com foco em **arquitetura moderna**, **boas práticas corporativas** e **facilidade de evolução**, sendo adequado para cenários de MVP, ambientes cloud-native e avaliações técnicas.

---

### Componentes principais

* **Spring Cloud Gateway (WebFlux)** – Gateway reativo
* **Spring Security OAuth2 Resource Server** – Validação de JWT
* **Keycloak** – Identity Provider (IdP)
* **Actuator** – Health, info e métricas
* **(Opcional)** Eureka / Service Discovery

---

## 🔐 Segurança

A segurança é baseada em **OAuth2 Resource Server** com **JWT emitido pelo Keycloak**.

### Fluxo

1. Cliente autentica no Keycloak
2. Recebe um `access_token` JWT
3. Envia o token no header:

```
Authorization: Bearer <token>
```

4. O Gateway:

   * Valida assinatura e issuer
   * Extrai roles do token
   * Aplica regras de autorização e rate limit

### Roles

* `ROLE_ADMIN`
* `ROLE_USER`

As roles são definidas no **realm fastorder** e propagadas via JWT.

---

## 🚦 Rate Limit por Perfil

Implementado via **GlobalFilter**:

* `ROLE_ADMIN` → **sem limite**
* Demais perfis → **100 requisições** por janela simples (in-memory)

> O filtro foi projetado para fácil substituição por Redis ou Bucket4j.

---

## 🔀 Roteamento

O gateway suporta:

* Roteamento estático via `application.yml`
* **Roteamento dinâmico via Admin API**

Exemplo de rota:

```
/api/orders/** → lb://order-service
```

---

## 🐳 Infraestrutura (Docker)

### Serviços

* PostgreSQL 15
* Keycloak 24

### Subida do ambiente

```bash
docker-compose up -d
```

Keycloak:

* URL: [http://localhost:8085](http://localhost:8085)
* Realm: `fastorder`

---

## 🧪 Testes

* Testes de contexto com `@SpringBootTest`
* Configuração de segurança isolada para testes
* JWT mockado quando necessário

---

## 🚀 Tecnologias Utilizadas

* Java 21+
* Spring Boot 3.x
* Spring Cloud Gateway (WebFlux)
* Spring Security
* OAuth2 / JWT
* Keycloak
* Docker / Docker Compose
