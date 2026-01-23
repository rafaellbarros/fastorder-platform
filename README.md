# FastOrder – API Gateway

## 📌 Visão Geral

O **FastOrder API Gateway** é um gateway reativo construído com **Spring Boot 3 + Spring Cloud Gateway (WebFlux)**, projetado para atuar como ponto único de entrada da plataforma FastOrder. Ele centraliza **roteamento**, **segurança**, **tratamento padronizado de erros** e **logging estruturado**, integrando-se com **Keycloak (OAuth2 / OpenID Connect)** e preparado para futura integração com **Service Discovery**.

O projeto segue princípios de **arquitetura cloud-native**, separação de responsabilidades e padrões utilizados em ambientes corporativos.

---

## 🧱 Arquitetura de Responsabilidades

| Camada                        | Responsabilidade                        |
| ----------------------------- | --------------------------------------- |
| **Spring Security (WebFlux)** | Autenticação e autorização              |
| **Security Filters**          | Auditoria de acesso (logs de segurança) |
| **Spring Cloud Gateway**      | Roteamento para microserviços           |
| **Global Filters**            | Interceptação de tráfego roteado        |
| **Exception Handlers**        | Padronização de respostas de erro       |

---

## 🔐 Segurança

A aplicação atua como **OAuth2 Resource Server**, validando **JWT emitido pelo Keycloak**.

### Fluxo de autenticação

1. Cliente autentica no **Keycloak**
2. Recebe um `access_token` JWT
3. Envia no header:

```
Authorization: Bearer <token>
```

4. O Gateway:

   * Valida assinatura do token
   * Valida `issuer`
   * Extrai roles de `realm_access.roles`
   * Aplica regras de autorização por endpoint

### Roles utilizadas

| Role         | Descrição                |
| ------------ | ------------------------ |
| `ROLE_ADMIN` | Acesso administrativo    |
| `ROLE_USER`  | Acesso padrão de usuário |

---

## 🚫 Tratamento Global de Exceções

Foi implementado um **GlobalExceptionHandler** para padronizar respostas de erro da API.

### Respostas de erro padronizadas

| Situação                          | HTTP | Estrutura de resposta |
| --------------------------------- | ---- | --------------------- |
| Falha de autenticação             | 401  | `UNAUTHORIZED`        |
| Acesso negado (role insuficiente) | 403  | `FORBIDDEN`           |
| Erro inesperado                   | 500  | `INTERNAL_ERROR`      |

### Exemplo de resposta

```json
{
  "error": "FORBIDDEN",
  "message": "Access Denied",
  "timestamp": "2026-01-23T11:43:55.225Z"
}
```

---

## 🧾 Logging Implementado

O projeto já possui **dois níveis de logging**, separados por responsabilidade.

### 1️⃣ Logging de Segurança (Security Layer)

Implementado via **WebFilter**, registrando:

* Método HTTP
* URL
* Status final
* Tempo de resposta

Exemplo:

```
SECURITY GET /admin/routes -> 403 FORBIDDEN (11 ms)
```

Esse log ocorre **mesmo quando a requisição é bloqueada pela segurança**.

---

### 2️⃣ Logging de Gateway (Roteamento)

Preparado via **GlobalFilter**, responsável por registrar:

* Chamadas que **foram roteadas para microserviços**
* Tempo de resposta do downstream

Importante:

> O `GlobalFilter` só é executado quando a requisição corresponde a uma **rota do Gateway**.
> Endpoints internos como `/admin/**`, `/actuator/**` e endpoints da própria aplicação **não passam pelo Gateway Filter Chain**.

---

## 🎨 Logs coloridos no console

Foi configurado **Logback com `logback-spring.xml`** utilizando conversores do Spring Boot para exibição colorida e legível em ambiente local.

Objetivo:

* Melhor leitura em desenvolvimento
* Preparação futura para logs estruturados (JSON + observabilidade)

---

## 🚦 Rate Limit por Perfil

Implementação inicial via **GlobalFilter** (in-memory):

| Perfil       | Limite                             |
| ------------ | ---------------------------------- |
| `ROLE_ADMIN` | Sem limite                         |
| Outros       | 100 requisições por janela simples |

Projetado para futura substituição por:

* Redis
* Bucket4j
* Rate limiting distribuído

---

## 🔀 Roteamento

Suporte a:

* Roteamento estático via `application.yml`
* Estrutura preparada para roteamento dinâmico via Admin API

Exemplo:

```
/api/orders/** → lb://order-service
```

---

## ❤️ Observabilidade (Base preparada)

O projeto já está organizado para evolução futura para:

* Correlation ID
* Tracing distribuído
* Logs estruturados
* Integração com stack de observabilidade (ELK, Grafana, etc.)

---

## 🐳 Infraestrutura (Docker)

### Serviços

* PostgreSQL 15
* Keycloak 24

Subida:

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
* Spring Security (OAuth2 Resource Server)
* JWT / Keycloak
* Logback
* Docker / Docker Compose
