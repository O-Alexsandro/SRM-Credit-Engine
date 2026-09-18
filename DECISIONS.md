# DECISIONS.md

# SRM Credit Engine - Decisões Técnicas

## 1. Objetivo

Este documento registra as principais decisões técnicas e de escopo adotadas durante o desenvolvimento do SRM Credit Engine.

O objetivo é deixar claro o que foi implementado, o que foi simplificado e os motivos dessas escolhas.

---

## 2. Arquitetura em camadas

Foi adotada uma arquitetura em camadas, separando:

* Controllers;
* Services;
* Repositories;
* Entities;
* DTOs.

A decisão foi tomada para manter as responsabilidades separadas e facilitar a manutenção e os testes.

A regra de negócio foi mantida principalmente na camada de Service, evitando concentrar lógica financeira nos Controllers.

---

## 3. Premissas adotadas diante de ambiguidades

Durante a análise do requisito, foram identificadas algumas regras que, em um projeto real, precisariam ser confirmadas com o negócio.

Essas questões foram registradas no `SPEC.md` e, para o escopo deste desafio, foram adotadas premissas explícitas para permitir a implementação.

Entre as principais decisões estão:

* Taxa base fixa em 1,0% ao mês;
* Prazo considerado em meses inteiros;
* Utilização da cotação USD/BRL com maior `effectiveAt` como cotação vigente mais recente;
* Arredondamento `HALF_EVEN` para 2 casas;
* Arredondamento do valor presente em BRL antes da conversão para USD.

As premissas podem ser alteradas futuramente caso o negócio defina regras diferentes.

---

## 4. Uso do Strategy Pattern

Foi utilizado o padrão Strategy para a precificação dos diferentes tipos de recebíveis.

Cada tipo possui sua própria estratégia de cálculo:

* `DuplicataPricingStrategy`;
* `ChequePricingStrategy`.

A decisão permite manter as regras de cada produto separadas e facilita a inclusão de novos tipos de recebíveis no futuro.

---

## 5. `BigDecimal` para valores financeiros

Foi escolhido `BigDecimal` para representar valores monetários e realizar os cálculos financeiros.

A decisão evita problemas de precisão associados a tipos de ponto flutuante e atende à necessidade de precisão decimal do domínio financeiro.

O arredondamento utiliza `RoundingMode.HALF_EVEN`.

---

## 6. Banco de dados relacional

Foi utilizado PostgreSQL como banco de dados.

A escolha atende ao requisito de persistência relacional e facilita a garantia de integridade entre as entidades.

O relacionamento entre recebível e liquidação possui restrição de unicidade, reforçando a regra de que um recebível pode ter no máximo uma liquidação.

---

## 7. Idempotência

A liquidação foi implementada de forma idempotente.

Quando um recebível já possui uma liquidação, uma nova solicitação retorna o registro existente em vez de criar outro.

Além do controle na aplicação, a tabela de liquidações possui uma restrição única sobre o recebível.

Essa decisão busca proteger contra duplicidade causada por retries ou chamadas repetidas.

---

## 8. Cotação cambial

Foi adotado o cadastro manual de cotações cambiais por meio da API.

Para uma liquidação em USD, o sistema busca a cotação USD/BRL com maior valor no campo `effectiveAt`, considerando-a como a cotação vigente mais recente.

A cotação efetivamente utilizada é armazenada junto à liquidação.

A integração com um provedor externo de câmbio não foi implementada nesta versão.

---

## 9. Política de conversão cambial

O cálculo é realizado inicialmente em BRL.

Quando a liquidação é em USD:

1. O valor presente é calculado em BRL;
2. O valor em BRL é arredondado para 2 casas decimais utilizando `HALF_EVEN`;
3. O valor é dividido pela cotação USD/BRL;
4. O resultado convertido é arredondado para 2 casas decimais.

Essa decisão foi adotada para seguir os golden cases fornecidos no desafio.

### 9.1 Deságio e valor presente em BRL

Foi decidido manter o `presentValueBrl` registrado na liquidação para preservar o valor presente calculado antes da conversão cambial.

O deságio é calculado pela diferença entre o valor de face e o valor presente em BRL:

```text
Deságio = Face Value - Present Value BRL
```

Essa abordagem mantém o deságio em BRL mesmo quando a liquidação é realizada em USD e evita calcular o deságio a partir do valor convertido.

---

## 10. Tratamento global de exceções

Foi adotado um tratamento global de exceções utilizando `@RestControllerAdvice`.

A decisão evita a repetição de tratamento de erros em cada Controller e permite padronizar as respostas da API.

Foram criadas exceções específicas para recursos não encontrados, além do tratamento de erros de validação.

---

## 11. Filtros de liquidação

O endpoint de consulta de liquidações suporta filtros por:

* Moeda;
* Cedente;
* Período.

Nesta versão, os filtros são processados pela aplicação após a consulta dos registros.


---

## 12. Transações

A operação de liquidação foi configurada como transacional.

A criação do settlement e a atualização do status do recebível fazem parte da mesma transação.

Dessa forma, em caso de falha, a operação pode sofrer rollback e evitar uma liquidação parcialmente persistida.

---

## 13. O que foi simplificado

Alguns itens não foram implementados nesta versão devido ao escopo definido para a entrega:

* Integração real com provedor externo de câmbio;
* Docker e Docker Compose;
* Paginação server-side;
* Observabilidade avançada;
* Controle distribuído de concorrência;
* Processamento assíncrono;
* Mensageria;
* Deploy em ambiente produtivo;
* Frontend completo.

As simplificações foram feitas para priorizar a corretude do motor de precificação, integridade dos dados, idempotência, testes e clareza da implementação.

---

## 14. Critério de priorização

Durante o desenvolvimento, a prioridade foi:

1. Implementar corretamente as regras financeiras;
2. Garantir precisão decimal;
3. Garantir a integridade da liquidação;
4. Cobrir os golden cases com testes;
5. Implementar tratamento de erros;
6. Manter o código simples e compreensível.

Funcionalidades consideradas de maior complexidade operacional foram deixadas para uma possível evolução futura.

---

## 15. Resultado

As decisões adotadas buscaram equilibrar corretude, simplicidade e o escopo esperado para a entrega.

O projeto prioriza uma implementação que seja fácil de testar, explicar e evoluir, evitando adicionar complexidade que não seja necessária para os requisitos atuais.
