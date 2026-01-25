# FastOrder Platform – API Gateway

## 📌 Visão Geral

O **FastOrder API Gateway** é um gateway reativo baseado em **Spring Boot 3 + Spring Cloud Gateway (WebFlux)** que atua como ponto único de entrada da plataforma **FastOrder**, responsável por:

* Roteamento para microserviços
* Validação de segurança via OAuth2 / JWT
* Integração com **Service Discovery (Eureka)**
* Tratamento padronizado de erros
* Logging estruturado
* Base para observabilidade distribuída

A solução segue princípios de **arquitetura de microsserviços cloud-native**, com separação clara entre **Gateway, serviços de domínio e infraestrutura**.

---

# 🧩 Arquitetura da Plataforma

```
[ Client / Frontend ]
          |
          v
[ API Gateway (WebFlux) ]
          |
          v
   lb://user-service
          |
          v
[ User Service (Spring MVC) ]
```

### Infraestrutura de suporte

| Componente        | Função                      |
| ----------------- | --------------------------- |
| **Eureka Server** | Service Discovery           |
| **Keycloak**      | Authorization Server (OIDC) |
| **Zipkin**        | Distributed Tracing         |
| **Prometheus**    | Métricas                    |
| **Actuator**      | Health + Metrics endpoints  |

---

# 🚪 Responsabilidades do Gateway

| Camada                        | Responsabilidade           |
| ----------------------------- | -------------------------- |
| **Spring Cloud Gateway**      | Roteamento reativo         |
| **Spring Security (WebFlux)** | Validação de JWT           |
| **Security Filters**          | Logging de segurança       |
| **Global Filters**            | Logging de tráfego roteado |
| **Exception Handlers**        | Padronização de erros      |

---

# 🔐 Segurança

O Gateway funciona como **OAuth2 Resource Server**, validando JWT emitido pelo **Keycloak**.

### Fluxo

1. Cliente autentica no Keycloak
2. Recebe JWT
3. Envia:

```
Authorization: Bearer <token>
```

4. Gateway:

   * Valida assinatura
   * Valida issuer
   * Extrai roles
   * Aplica autorização

### Configuração principal

```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri:
  http://localhost:8085/realms/fastorder
```

---

## Roles

| Role         | Uso           |
| ------------ | ------------- |
| `ROLE_ADMIN` | Administração |
| `ROLE_USER`  | Acesso padrão |

---

# 🔄 Service Discovery

O Gateway **não usa URL fixa**. Ele descobre instâncias dinamicamente via **Eureka**:

```yaml
eureka.client.service-url.defaultZone: http://localhost:8761/eureka
```

Roteamento:

```
/api/users/** → lb://user-service
```

O **Spring Cloud LoadBalancer** resolve a instância ativa.

---

# 🧾 Logging

## 1️⃣ Logging de Segurança

Executado mesmo quando a requisição é bloqueada.

Exemplo:

```
SECURITY GET /admin/routes -> 403 FORBIDDEN (9 ms)
```

## 2️⃣ Logging de Gateway

Executado apenas quando a requisição é roteada para outro serviço.

---

# 🚫 Tratamento Global de Erros

Respostas padronizadas:

| Situação          | HTTP |
| ----------------- | ---- |
| Token inválido    | 401  |
| Role insuficiente | 403  |
| Erro inesperado   | 500  |

Exemplo:

```json
{
  "error": "FORBIDDEN",
  "message": "Access Denied",
  "timestamp": "2026-01-23T11:43:55.225Z"
}
```

---

# ❤️ Observabilidade

Preparado para:

* Métricas Prometheus
* Tracing com Zipkin
* Actuator health checks
* Logs estruturados (evolução futura)

---

# ⚙ Perfis de Execução

| Profile   | Infra obrigatória          | Uso                      |
| --------- | -------------------------- | ------------------------ |
| **local** | Keycloak + Eureka + Zipkin | Ambiente completo Docker |
| **dev**   | Nenhuma                    | Desenvolvimento rápido   |
| **test**  | Nenhuma                    | Testes automatizados     |

Execução:

```
-Dspring.profiles.active=local
```

---

# 🐳 Infraestrutura Docker

Serviços:

| Serviço  | Porta |
| -------- | ----- |
| Keycloak | 8085  |
| Eureka   | 8761  |
| Zipkin   | 9411  |

Subida:

```bash
docker compose -f docker/docker-compose.yml up
docker compose -f docker/docker-compose-observability.yml up
```

---

# 🧪 Testes

* `@SpringBootTest` com profile `test`
* Feign clients mockados
* `JwtDecoder` mockado
* Infra externa desabilitada

---

# 🚀 Tecnologias

* Java 21
* Spring Boot 3
* Spring Cloud Gateway
* Spring Security OAuth2 Resource Server
* Eureka Discovery
* OpenFeign + LoadBalancer
* Keycloak
* Prometheus
* Zipkin
* Docker


