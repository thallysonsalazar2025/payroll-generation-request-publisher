# Responsabilidades do componente `payroll-generation-request-publisher`

## Objetivo principal
Receber uma requisição de geração de holerite vinda do gateway e publicar um evento/comando em um tópico/fila do RabbitMQ para que o componente gerador processe a solicitação.

## Responsabilidades recomendadas

### 1) Validação de contrato da requisição
Além de validar o objeto (`month`, `year`), o componente deve:
- Garantir que `month` esteja entre 1 e 12.
- Garantir que `year` esteja em faixa aceitável (ex.: >= 2000 e <= ano atual + 1).
- Rejeitar payload inválido com erro claro (HTTP 400).
- Validar formato/obrigatoriedade de identificadores necessários (ex.: `employeeId`, `companyId`, `requesterId`).

### 2) Validação de regra de negócio mínima (pré-publicação)
- Verificar se o período solicitado é permitido pela política (ex.: sem data futura indevida).
- Garantir que o endpoint é para **um único mês/ano por requisição**.
- Opcional: bloquear reprocessamento simultâneo do mesmo `employeeId + month + year` quando houver job em andamento.

### 3) Idempotência
Responsabilidade importante para evitar mensagens duplicadas:
- Aceitar/gerar `idempotencyKey`.
- Persistir controle de chave por uma janela de tempo.
- Em repetição da mesma requisição, responder de forma consistente sem republicar mensagem.

### 4) Publicação confiável no RabbitMQ
- Declarar exchange/routing key/fila conforme contrato.
- Publicar com `message persistence` e headers mínimos.
- Confirmar publicação (publisher confirms) antes de retornar sucesso ao cliente.
- Em falha de publicação, retornar erro apropriado (ex.: HTTP 503/500) e registrar causa.

### 5) Enriquecimento e padronização da mensagem
Produzir mensagem com metadados úteis para rastreabilidade:
- `requestId` / `correlationId`.
- `occurredAt` (timestamp).
- `source` (serviço de origem).
- `schemaVersion`.
- Contexto de auditoria (`requestedBy`, tenant, etc.).

### 6) Observabilidade
- Logs estruturados com correlação ponta a ponta.
- Métricas: quantidade de requests, taxa de validação inválida, taxa de publicação com sucesso/erro, latência.
- Tracing distribuído (quando aplicável).

### 7) Segurança e compliance
- Validar identidade/autorização no nível esperado pelo domínio (mesmo com gateway na frente, se necessário).
- Sanitizar dados sensíveis em logs.
- Aplicar princípio do menor privilégio para credenciais do RabbitMQ.

### 8) Resiliência operacional
- Timeout e retry controlado na publicação (com backoff).
- Circuit breaker (se fizer sentido no stack).
- Estratégia para indisponibilidade do broker (degradação + resposta clara).

### 9) Contrato de resposta para o cliente
Em vez de esperar geração síncrona, responder padrão assíncrono:
- `202 Accepted` com `requestId` e status inicial (`RECEIVED`/`QUEUED`).
- Endpoint complementar de consulta de status (se existir no ecossistema).

### 10) Governança de contrato de evento
- Versionar schema de mensagem.
- Garantir retrocompatibilidade entre publisher e consumer.
- Documentar contrato (campos obrigatórios/opcionais e semântica).

## O que **não** deve ser responsabilidade deste componente
- Geração do arquivo do holerite.
- Cálculo detalhado de folha.
- Regras pesadas do processamento final (essas ficam no consumer/gerador).

## Checklist mínimo (MVP robusto)
- [ ] Validação de `month/year` + identificadores obrigatórios.
- [ ] Regra de 1 período por requisição.
- [ ] Idempotência por chave de negócio.
- [ ] Publicação com confirmação no RabbitMQ.
- [ ] `correlationId` + logs estruturados.
- [ ] Resposta `202 Accepted` com `requestId`.
- [ ] Métricas básicas e tratamento de erro consistente.

## Sugestão prática
Se o seu escopo atual já cobre validação e publicação, as **próximas prioridades** deveriam ser:
1. Idempotência.
2. Observabilidade (correlationId + métricas).
3. Contrato assíncrono de resposta (`202 + requestId`).

Esses três pontos costumam evitar retrabalho e incidentes em produção quando o volume aumenta.
