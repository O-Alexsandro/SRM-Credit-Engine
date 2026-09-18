# SRM Credit Engine

API REST para precificação e liquidação de recebíveis em BRL e USD.

O projeto foi desenvolvido como solução para o desafio técnico **SRM Credit Engine**, com foco em precisão financeira, organização em camadas, idempotência e testes automatizados.

---

## 1. Sobre o projeto

O SRM Credit Engine permite:

* Cadastro de cedentes;
* Cadastro de recebíveis;
* Cadastro manual de cotações cambiais;
* Simulação de precificação com valor presente e deságio;
* Liquidação de recebíveis;
* Consulta de liquidações;
* Filtros por moeda, cedente e período;
* Registro da cotação utilizada na liquidação;
* Tratamento global de exceções.

O cálculo financeiro utiliza `BigDecimal` e `RoundingMode.HALF_EVEN`, evitando o uso de `float` ou `double` para valores monetários.

---

## 2. Tecnologias utilizadas

* Java 17;
* Spring Boot;
* Spring Web;
* Spring Data JPA;
* Bean Validation;
* PostgreSQL;
* JUnit 5;
* Mockito;
* Maven;
* OpenAPI/Swagger.

---

## 3. Arquitetura

O projeto utiliza uma arquitetura em camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

### Principais responsabilidades

**Controller**

Responsável por receber as requisições HTTP, validar os dados de entrada e retornar as respostas da API.

**Service**

Responsável pelas regras de negócio e pelo fluxo das operações.

**Repository**

Responsável pelo acesso ao banco de dados utilizando Spring Data JPA.

**Entity**

Representa as entidades persistidas no banco.

**DTO**

Define os objetos utilizados na entrada e saída da API.

**Strategy**

Utilizada na precificação para separar as regras de cálculo de cada tipo de recebível.

---

## 4. Estrutura do projeto

```text
src/main/java/com/srm/credit/engine/
├── cedente/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── exception/
│
├── receivable/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── exception/
│
├── exchange/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
│
├── pricing/
│   ├── strategy/
│   ├── service/
│   ├── dto/
│   └── controller/
│
└── settlement/
    ├── entity/
    ├── repository/
    ├── dto/
    ├── service/
    └── controller/
```

---

## 5. Regras de precificação

A fórmula utilizada é:

```text
PV = Face Value / (1 + Base Rate + Spread)^Term
```

### Taxa base

```text
1,0% ao mês
```

### Spreads

| Tipo                | Spread    |
| ------------------- | --------- |
| Duplicata Mercantil | 1,5% a.m. |
| Cheque Pré-datado   | 2,5% a.m. |

Os cálculos são realizados utilizando `BigDecimal`.

O deságio é calculado pela diferença entre o valor de face e o valor presente em BRL:

```text
Deságio = Face Value - Valor Presente em BRL
```

O arredondamento financeiro utiliza:

```text
RoundingMode.HALF_EVEN
```

### Conversão para USD

Quando a liquidação é realizada em USD:

1. O valor presente é calculado em BRL;
2. O valor em BRL é arredondado para 2 casas;
3. O valor é dividido pela cotação USD/BRL;
4. O resultado em USD é arredondado para 2 casas.

Para a liquidação em USD, é utilizada a cotação USD/BRL com maior `effectiveAt`.

---

## 6. Como executar o projeto

### Pré-requisitos

É necessário ter instalado:

* Java 17;
* Maven;
* PostgreSQL.

### Banco de dados

Crie um banco PostgreSQL chamado:

```text
srmcredit
```

A aplicação utiliza as seguintes propriedades:

```yaml
spring:
  datasource:
    url: ${DATASOURCE.SRMCREDIT}
    username: ${DATASOURCE.USERNAME}
    password: ${DATASOURCE.PASSWORD}
```

Configure as variáveis de ambiente:

```text
DATASOURCE.SRMCREDIT=jdbc:postgresql://localhost:5432/srmcredit
DATASOURCE.USERNAME=postgres
DATASOURCE.PASSWORD=sua_senha
```


### Executando

Na raiz do projeto:

```bash
mvn spring-boot:run
```

---

## 7. Endpoints principais

### Cedentes

Criar cedente:

```http
POST /cedentes
```

Consultar cedentes:

```http
GET /cedentes
```

Consultar por ID:

```http
GET /cedentes/{id}
```

Exemplo:

```json
{
  "name": "Empresa Exemplo",
  "document": "12345678000100"
}
```

---

### Recebíveis

Criar recebível:

```http
POST /receivables
```

Consultar recebíveis:

```http
GET /receivables
```

Consultar por ID:

```http
GET /receivables/{id}
```

Exemplo:

```json
{
  "cedenteId": 1,
  "type": "DUPLICATA_MERCANTIL",
  "faceValue": 100000.00,
  "term": 3,
  "paymentCurrency": "BRL"
}
```

---

### Cotações

Criar cotação:

```http
POST /exchange-rates
```

Consultar cotações:

```http
GET /exchange-rates
```

Consultar por ID:

```http
GET /exchange-rates/{id}
```

Exemplo:

```json
{
  "fromCurrency": "USD",
  "toCurrency": "BRL",
  "rate": 5.4321,
  "effectiveAt": "2026-09-14T16:00:00"
}
```

---

### Precificação

Simular uma precificação:

```http
POST /pricing
```

Exemplo em BRL:

```json
{
  "type": "DUPLICATA_MERCANTIL",
  "faceValue": 100000.00,
  "term": 3,
  "paymentCurrency": "BRL"
}
```

Exemplo em USD:

```json
{
  "type": "DUPLICATA_MERCANTIL",
  "faceValue": 100000.00,
  "term": 3,
  "paymentCurrency": "USD",
  "exchangeRate": 5.4321
}
```

A resposta da precificação inclui o valor presente e o deságio. Em uma operação USD, o deságio permanece em BRL.

Exemplo de resposta:

```json
{
  "faceValue": 100000.00,
  "presentValue": 17094.67,
  "discount": 7140.06,
  "paymentCurrency": "USD"
}
```

---

### Liquidação

Criar uma liquidação:

```http
POST /settlements
```

Exemplo:

```json
{
  "receivableId": 1,
  "currency": "BRL"
}
```

Consultar liquidações:

```http
GET /settlements
```

Consultar por ID:

```http
GET /settlements/{id}
```

Também é possível utilizar filtros:

```http
GET /settlements?currency=USD
```

```http
GET /settlements?cedenteId=1
```

```http
GET /settlements?from=2026-09-01T00:00:00&to=2026-09-30T23:59:59
```

Os filtros podem ser combinados:

```http
GET /settlements?currency=USD&cedenteId=1
```

---

## 8. Idempotência

A liquidação é idempotente.

Um recebível pode possuir no máximo uma liquidação.

Quando uma nova solicitação é realizada para um recebível que já está liquidado, o sistema retorna a liquidação existente em vez de criar um novo registro.

A restrição de unicidade no banco também reforça essa regra.

---

## 9. Tratamento de exceções

A aplicação possui tratamento global de exceções utilizando `@RestControllerAdvice`.

Exemplo de recurso inexistente:

```json
{
  "status": 404,
  "message": "Recebível não encontrado",
  "timestamp": "2026-09-16T12:00:00"
}
```

Erros de validação retornam `400 Bad Request`.

Recursos inexistentes retornam `404 Not Found`.

---

## 10. Testes

O projeto possui testes unitários e testes de controller.

São cobertos, entre outros:

* Regras de precificação;
* Golden Cases;
* Conversão cambial;
* Cadastro e consulta;
* Liquidação em BRL;
* Liquidação em USD;
* Idempotência;
* Recursos inexistentes;
* Validações;
* Filtros de liquidação;
* Tratamento dos controllers.

Para executar os testes:

```bash
mvn test
```

---

## 11. Golden Cases

### C1 — Duplicata em BRL

```text
Face Value: R$ 100.000,00
Prazo: 3 meses
Tipo: Duplicata Mercantil
Moeda: BRL

Valor presente: R$ 92.859,94
Deságio: R$ 7.140,06
```

### C2 — Cheque Pré-datado em BRL

```text
Face Value: R$ 25.000,00
Prazo: 2 meses
Tipo: Cheque Pré-datado
Moeda: BRL

Valor presente: R$ 23.337,77
Deságio: R$ 1.662,23
```

### C3 — Duplicata em USD

```text
Face Value: R$ 100.000,00
Prazo: 3 meses
Tipo: Duplicata Mercantil
Cotação USD/BRL: 5,4321
Moeda: USD

Valor presente em BRL:
R$ 92.859,94

Deságio em BRL:
R$ 7.140,06

Valor liquidado em USD:
US$ 17.094,67
```

Os três casos são validados automaticamente pelos testes.

---

## 12. Documentação

A documentação do projeto está dividida nos seguintes arquivos:

### `SPEC.md`

Contém as premissas, regras de negócio, precisão, arredondamento, critérios de aceite e questões que seriam confirmadas com o negócio.

### `DECISIONS.md`

Registra as principais decisões técnicas e as simplificações adotadas durante o desenvolvimento.

### `REVIEW.md`

Contém a análise do código fornecido no Anexo A do desafio.

### `AI_USAGE.md`

Descreve como a IA foi utilizada durante o desenvolvimento, incluindo validações, refinamentos e decisões que permaneceram sob responsabilidade do desenvolvedor.

### Swagger / OpenAPI

A documentação interativa da API está disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 13. Escopo

### Implementado

* API REST;
* PostgreSQL;
* CRUD de cedentes;
* CRUD de recebíveis;
* CRUD de cotações;
* Precificação com Strategy Pattern;
* Conversão BRL/USD;
* Liquidação;
* Idempotência;
* Filtros de liquidação;
* Auditoria da cotação utilizada;
* Tratamento global de exceções;
* Testes automatizados.

### Não implementado nesta versão

* Integração real com provedor externo de câmbio;
* Mensageria;
* Processamento assíncrono;
* Observabilidade avançada;
* Controle distribuído de concorrência;
* Deploy em ambiente produtivo.

As simplificações e os motivos para elas estão registrados no `DECISIONS.md`.

---

## 14. Decisões principais

As principais decisões do projeto foram:

* PostgreSQL como banco relacional;
* `BigDecimal` para cálculos financeiros;
* `HALF_EVEN` como política de arredondamento;
* Strategy Pattern para as regras de precificação;
* Cotação USD/BRL mais recente definida pelo maior `effectiveAt`;
* Liquidação transacional;
* Idempotência reforçada por restrição de unicidade;
* Tratamento global de exceções.

Mais detalhes estão disponíveis no `DECISIONS.md`.

---

## 15. Autor

Desenvolvido como parte do desafio técnico SRM Credit Engine.