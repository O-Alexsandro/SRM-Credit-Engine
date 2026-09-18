# AI_USAGE.md

# SRM Credit Engine - Uso de IA

## 1. Objetivo

A IA foi utilizada como ferramenta de apoio durante o desenvolvimento do SRM Credit Engine, principalmente para acelerar a implementação, esclarecer dúvidas e auxiliar na revisão do projeto.

Seu uso teve como objetivo aumentar a produtividade durante o desenvolvimento, sem substituir a análise, a validação ou as decisões técnicas realizadas pelo desenvolvedor.

A IA foi utilizada principalmente para:

* Implementação dos CRUDs do projeto;
* Apoio em dúvidas financeiras e cálculos;
* Sugestão de testes unitários;
* Revisão de código;
* Identificação e resolução de problemas de implementação;
* Apoio na documentação e análise dos requisitos.
* Criação do frontend em React

---

## 2. Como a IA foi utilizada

A utilização da IA aconteceu de forma incremental ao longo do desenvolvimento do projeto.

No início, a IA foi utilizada como apoio na implementação dos CRUDs, auxiliando na estruturação de entidades, DTOs, repositories etc.

Durante o desenvolvimento, também foi utilizada para esclarecer dúvidas relacionadas a cálculos e conceitos financeiros, além de sugerir testes unitários e testes de controller para validar o comportamento da aplicação.

A IA também foi utilizada durante a revisão dos códigos, ajudando a identificar possíveis problemas de implementação e sugerindo alternativas para corrigi-los.

Por fim, foi utilizada como apoio na análise dos requisitos do desafio e na elaboração e revisão da documentação do projeto.

As sugestões fornecidas pela IA não foram utilizadas de forma automática. Cada alteração foi analisada, implementada e validada no projeto antes de ser considerada concluída.

### 2.1 Specs e prompts estratégicos

Para evitar tratar todo o desafio de uma única vez, o desenvolvimento foi dividido em etapas. A cada etapa, foi apresentado à IA o objetivo da funcionalidade e o contexto necessário para sua implementação, validação ou revisão.

O fluxo utilizado foi:

```text
1. Análise do desafio
   ↓
2. Configuração inicial do projeto e banco
   ↓
3. CRUD de Cedente
   ↓
4. CRUD de Recebível
   ↓
5. CRUD de Cotação Cambial
   ↓
6. Motor de Precificação
   ↓
7. Testes e validação dos Golden Cases
   ↓
8. Liquidação, Idempotência e Deságio
   ↓
9. Filtros de Liquidação
   ↓
10. Tratamento Global de Exceções
   ↓
11. Documentação
   ↓
12. Revisão final
```

Alguns exemplos de direcionamentos utilizados:

**Sugestão de testes**

> Sugerir testes unitários e de controller para a funcionalidade implementada, cobrindo cenários de sucesso, validação e recursos inexistentes.

**Resolução de problemas**

> Analisar o erro apresentado na implementação, identificar a causa provável e sugerir uma correção compatível com a arquitetura atual do projeto.

**Revisão de código**

> Revisar o código implementado e identificar possíveis problemas de implementação, responsabilidades, validações, tratamento de erros ou inconsistências com os requisitos.

**Análise de requisitos e documentação**

> Analisar os requisitos do desafio e ajudar a documentar as premissas, decisões técnicas, escopo e pontos que precisam ser considerados na implementação.

A divisão por etapas permitiu implementar e validar cada funcionalidade antes de avançar para a seguinte, reduzindo alterações simultâneas e facilitando a identificação de problemas.

---

## 3. Validação das respostas da IA

As sugestões da IA foram validadas antes de serem incorporadas ao projeto.

As principais formas utilizadas para essa validação foram:

* Execução dos testes automatizados;
* Validação dos Golden Cases;
* Execução da aplicação localmente;
* Testes manuais utilizando a API;
* Comparação da implementação com os requisitos do desafio;
* Revisão do código após cada alteração relevante.

Essa validação foi especialmente importante em partes relacionadas a cálculos financeiros, precisão decimal, conversão cambial e liquidação.

O resultado dos testes e o comportamento observado na API foram utilizados para confirmar ou ajustar as sugestões recebidas.

---

## 4. Caso concreto identificado durante a validação

Durante os testes manuais da API de liquidação, foi identificado um problema na representação dos valores retornados pela aplicação.

O cálculo estava correto, porém a API retornava os valores com mais casas decimais do que o formato definido para a resposta.

Um exemplo observado foi:

```json
{
  "id": 2,
  "receivableId": 2,
  "amount": 17094.670000,
  "currency": "USD",
  "fxRateUsed": 5.432100,
  "settledAt": "2026-09-16T12:20:24.887621"
}
```

Após a identificação do problema, o código foi ajustado para apresentar:

* Valores monetários com 2 casas decimais;
* Cotação cambial com 4 casas decimais.

O ajuste foi realizado no mapeamento da entidade para o DTO de resposta, utilizando `BigDecimal.setScale()` com `RoundingMode.HALF_EVEN`.

A alteração foi validada novamente por meio da execução da API e dos testes automatizados.

Esse caso reforçou a importância de validar o comportamento da aplicação na prática, e não considerar uma implementação concluída apenas porque o código compila ou os testes iniciais passam.

---

## 5. Refinamentos realizados durante o desenvolvimento

Além do caso de validação apresentado anteriormente, algumas decisões foram refinadas ao longo do desenvolvimento.

### 5.1 Seleção da cotação mais recente

Durante a implementação da liquidação em USD, foi necessário definir qual cotação deveria ser utilizada quando existissem várias cotações cadastradas.

Após analisar o requisito e o campo `effectiveAt`, foi definido que a cotação utilizada seria aquela com maior `effectiveAt`, considerando-a como a cotação vigente mais recente.

Essa decisão foi posteriormente registrada no `SPEC.md` e no `DECISIONS.md`.

### 5.2 Arredondamento antes da conversão

Durante a revisão da documentação e da implementação, também foi necessário deixar explícito o comportamento das operações cross-currency.

O fluxo definido foi:

1. Calcular o valor presente em BRL mantendo precisão intermediária;
2. Arredondar o valor em BRL para 2 casas utilizando `HALF_EVEN`;
3. Converter o valor arredondado para USD;
4. Arredondar o resultado convertido para 2 casas.

A documentação foi ajustada para representar exatamente esse comportamento e manter alinhamento com os Golden Cases.

### 5.3 Inclusão do deságio na precificação e liquidação

Durante a evolução da funcionalidade de liquidação, foi necessário preservar o valor presente em BRL (`presentValueBrl`) para permitir o cálculo explícito do deságio, inclusive em operações liquidadas em USD.

O fluxo validado foi:

1. Calcular o valor presente em BRL;
2. Arredondar o valor presente em BRL para 2 casas com `HALF_EVEN`;
3. Calcular o deságio como `Face Value - Present Value BRL`;
4. Quando aplicável, converter o valor presente em BRL para USD;
5. Validar os valores por testes automatizados e execução da API.

Essa alteração foi refletida na resposta de pricing, na liquidação e na interface de simulação e histórico.

---

## 6. O que não foi delegado à IA

Algumas decisões foram mantidas sob responsabilidade do desenvolvedor, principalmente aquelas relacionadas à definição do comportamento do sistema e ao escopo da solução.

Entre elas:

* Definição das premissas adotadas no projeto;
* Escolha da arquitetura em camadas;
* Decisão de utilizar Strategy para a precificação;
* Escolha do PostgreSQL;
* Definição da política de arredondamento;
* Definição de como a cotação cambial seria selecionada;
* Priorização das funcionalidades;
* Definição do que seria implementado ou deixado fora do escopo;
* Validação final do código e dos resultados.

A IA foi utilizada como ferramenta de apoio, mas as decisões finais foram tomadas pelo desenvolvedor após análise e validação das alternativas.

---

## 7. Princípios utilizados

Durante o desenvolvimento, foram adotados os seguintes princípios para o uso da IA:

* Não utilizar código sem compreender seu funcionamento;
* Validar alterações por meio de testes;
* Comparar os resultados com os requisitos do desafio;
* Revisar sugestões relacionadas a cálculos financeiros;
* Preferir soluções simples e compatíveis com o escopo do projeto;
* Manter a responsabilidade pelas decisões técnicas com o desenvolvedor.

---

## 8. Resultado

A IA contribuiu para acelerar a implementação, a resolução de problemas, os testes e a revisão do projeto.

Ao mesmo tempo, o código e as decisões finais foram validados pelo desenvolvedor por meio de testes automatizados, execução local, testes manuais e comparação com os requisitos e Golden Cases do desafio.

Dessa forma, a IA foi utilizada como uma ferramenta de engenharia para apoiar o desenvolvimento, e não como substituição da compreensão do código ou da tomada de decisões técnicas.