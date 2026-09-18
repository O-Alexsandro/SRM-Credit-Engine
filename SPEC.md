# SRM Credit Engine - Especificação Funcional

## 1. Objetivo

O SRM Credit Engine é uma API responsável por calcular o valor presente de recebíveis e realizar sua liquidação em BRL ou USD.

O sistema permite:

* Cadastro de cedentes;
* Cadastro de recebíveis;
* Cadastro manual de cotações cambiais;
* Simulação de precificação;
* Liquidação de recebíveis;
* Consulta de liquidações com filtros;
* Registro dos valores e da cotação utilizada na liquidação.

---

## 2. Premissas de Negócio

### 2.1 Moedas suportadas

O sistema suporta:

* BRL — Real brasileiro;
* USD — Dólar americano.

A moeda base utilizada nos cálculos financeiros é BRL.

Quando a liquidação é solicitada em USD, o cálculo do valor presente é realizado inicialmente em BRL e convertido para USD ao final.

---

### 2.2 Tipos de recebíveis

São suportados os seguintes tipos:

* `DUPLICATA_MERCANTIL`;
* `CHEQUE_PRE_DATADO`.

Cada tipo possui um spread específico aplicado à taxa base.

---

### 2.3 Perguntas ao negócio

Em um projeto real, algumas regras deveriam ser confirmadas com as áreas de negócio antes da implementação definitiva:

* A taxa base de 1,0% ao mês é fixa ou pode variar ao longo do tempo?
* Os spreads dos tipos de recebíveis são sempre fixos ou podem ser alterados por produto, cliente ou operação?
* O prazo do recebível deve ser informado sempre em meses inteiros ou pode possuir fração de mês?
* Para uma liquidação em USD, a cotação deve ser a mais recente disponível, a vigente no momento da operação ou deve considerar a data de vencimento do recebível?
* Existe algum critério adicional para considerar uma cotação como válida, além de sua data de efetividade?
* Uma liquidação já realizada pode ser estornada ou corrigida? Em caso positivo, como deve ser registrada essa alteração para manter a auditoria?
* O recebível pode ser liquidado em uma moeda diferente da sua moeda original em todos os casos ou existem restrições de negócio?
* Existem limites mínimos ou máximos para valor, prazo ou quantidade de liquidações?
* Como devem ser tratados casos em que duas solicitações de liquidação do mesmo recebível ocorrerem simultaneamente?
* O extrato de liquidações deverá possuir paginação e ordenação específicas quando o volume de registros crescer?

---

## 3. Regra de Precificação

A fórmula utilizada é:

```text
PV = Face Value / (1 + Base Rate + Spread)^Term
```

Onde:

* `PV` = valor presente;
* `Face Value` = valor de face do recebível;
* `Base Rate` = taxa base mensal;
* `Spread` = taxa adicional conforme o tipo de recebível;
* `Term` = prazo em meses.

### 3.1 Taxas utilizadas

| Tipo de recebível   | Taxa base | Spread    |
| ------------------- | --------- | --------- |
| Duplicata Mercantil | 1,0% a.m. | 1,5% a.m. |
| Cheque Pré-datado   | 1,0% a.m. | 2,5% a.m. |

### 3.2 Taxas efetivas

Para duplicata:

```text
1 + 0,01 + 0,015 = 1,025
```

Para cheque:

```text
1 + 0,01 + 0,025 = 1,035
```

### 3.3 Deságio

O deságio é calculado pela diferença entre o valor de face do recebível e o valor presente em BRL:

```text
Deságio = Face Value - Valor Presente em BRL
```

Para operações em USD, o deságio permanece expresso em BRL. O valor presente em BRL é calculado e arredondado antes da conversão para USD.

---

## 4. Precisão e Arredondamento

Os cálculos monetários utilizam `BigDecimal`.

Não são utilizados `float` ou `double` para valores financeiros.

A estratégia adotada é:

* Manter precisão intermediária durante o cálculo;
* Aplicar arredondamento `HALF_EVEN`;
* Arredondar o resultado final para 2 casas decimais.

O valor presente em BRL é arredondado para 2 casas decimais antes de uma eventual conversão cambial.

Na conversão cambial, o resultado convertido também é apresentado com 2 casas decimais.

---

## 5. Conversão Cambial

Quando a moeda de pagamento é BRL, não é necessária cotação.

Quando a moeda de pagamento é USD, é obrigatória uma cotação USD/BRL válida.

O fluxo de conversão é:

1. Calcular o valor presente inicialmente em BRL mantendo precisão intermediária;
2. Arredondar o valor presente em BRL para 2 casas decimais utilizando `HALF_EVEN`;
3. Dividir o valor em BRL pela cotação USD/BRL;
4. Arredondar o valor convertido para 2 casas decimais utilizando `HALF_EVEN`.

A conversão utilizada é:

```text
Valor em USD = Valor em BRL / Cotação USD/BRL
```

A cotação utilizada na liquidação é armazenada no registro de settlement para fins de auditoria.

---

## 6. Cotação Cambial

As cotações são cadastradas manualmente por meio da API.

Cada cotação possui:

* Moeda de origem;
* Moeda de destino;
* Valor da cotação;
* Data e hora de efetividade.

Para liquidações em USD, o sistema utiliza a cotação USD/BRL com maior valor no campo `effectiveAt`, considerando-a como a cotação vigente mais recente.

---

## 7. Liquidação

A liquidação de um recebível:

1. Localiza o recebível;
2. Verifica seu status;
3. Obtém a cotação, quando necessário;
4. Calcula o valor presente;
5. Calcula o deságio a partir do valor presente em BRL;
6. Cria o registro de settlement;
7. Armazena o valor presente em BRL (`presentValueBrl`);
8. Armazena o valor liquidado (`amount`);
9. Armazena a moeda utilizada;
10. Armazena a cotação utilizada, quando aplicável;
11. Atualiza o status do recebível para `SETTLED`.

---

## 8. Idempotência

Um recebível pode possuir no máximo uma liquidação.

Essa regra é reforçada pelo relacionamento único entre `settlement` e `receivable`.

Quando uma solicitação de liquidação é feita para um recebível que já está liquidado, o sistema retorna a liquidação existente em vez de criar uma nova.

Isso evita duplicidade de liquidações.

---

## 9. Status do Recebível

Os status suportados são:

* `AVAILABLE` — recebível disponível para liquidação;
* `SETTLED` — recebível já liquidado.

Somente recebíveis disponíveis devem gerar uma nova liquidação.

---

## 10. Auditoria

O registro de settlement armazena:

* Identificador da liquidação;
* Identificador do recebível;
* Valor de face;
* Valor presente em BRL;
* Deságio;
* Valor liquidado;
* Moeda da liquidação;
* Cotação cambial efetivamente utilizada;
* Data e hora da liquidação.

A cotação utilizada é preservada no settlement, mesmo que novas cotações sejam cadastradas posteriormente.

---

## 11. Golden Cases

### Caso 1 — Duplicata em BRL

```text
Face Value: R$ 100.000,00
Prazo: 3 meses
Tipo: Duplicata Mercantil
Moeda: BRL
```

Resultado esperado:

```text
R$ 92.859,94
```

### Caso 2 — Cheque em BRL

```text
Face Value: R$ 25.000,00
Prazo: 2 meses
Tipo: Cheque Pré-datado
Moeda: BRL
```

Resultado esperado:

```text
R$ 23.337,77
```

### Caso 3 — Duplicata em USD

```text
Face Value: R$ 100.000,00
Prazo: 3 meses
Tipo: Duplicata Mercantil
Cotação USD/BRL: 5,4321
Moeda: USD
```

Resultados esperados:

```text
Valor presente em BRL: R$ 92.859,94
Valor liquidado em USD: US$ 17.094,67
Desconto em BRL: R$ 7.140,06
```

---

## 12. Validações

A API valida:

* Campos obrigatórios;
* Valores positivos;
* Prazos positivos;
* Moedas suportadas;
* Tipos de recebíveis suportados;
* Existência do cedente;
* Existência do recebível;
* Existência de cotação para liquidação em USD.

Erros de validação retornam HTTP 400.

Recursos inexistentes retornam HTTP 404.

---

## 13. Escopo Atual

O projeto contempla:

* API REST;
* Persistência relacional em PostgreSQL;
* Precificação com Strategy Pattern;
* Liquidação idempotente;
* Consultas com filtros;
* Testes unitários e de controller;
* Documentação OpenAPI/Swagger;
* Tratamento global de exceções.

Itens não contemplados nesta versão:

* Integração real com provedores externos de câmbio;
* Mensageria;
* Processamento assíncrono;
* Observabilidade avançada;
* Controle distribuído de concorrência;
* Deploy em ambiente produtivo.
