# REVIEW.md

# SRM Credit Engine - Code Review

## Objetivo

Este documento apresenta a revisão do endpoint de liquidação disponibilizado no Anexo A do desafio.

Os problemas foram organizados por ordem de severidade, considerando principalmente impacto financeiro, integridade dos dados e segurança da aplicação.

---

## 1. SQL Injection

### Problema

O código monta as queries SQL diretamente utilizando valores recebidos da requisição:

```typescript
`SELECT * FROM receivables WHERE id = ${receivableId}`
```

e:

```typescript
`INSERT INTO settlements (receivable_id, amount, currency)
 VALUES (${receivableId}, ${finalAmount.toFixed(2)}, '${currency}')`
```

### Impacto

Um valor manipulado pelo cliente pode alterar a instrução SQL executada pelo banco.

Em produção, isso pode permitir acesso ou alteração indevida de dados.

### Correção proposta

Utilizar queries parametrizadas ou um mecanismo de acesso a dados que faça o binding dos parâmetros.

Também deve existir validação dos dados recebidos pela API.

---

## 2. Falta de transação na liquidação

### Problema

A criação do settlement e a atualização do recebível são realizadas em operações separadas:

```typescript
INSERT INTO settlements ...
```

e depois:

```typescript
UPDATE receivables SET status = 'SETTLED' ...
```

O código não utiliza uma transação envolvendo as duas operações.

### Impacto

Pode ocorrer uma liquidação parcialmente persistida.

Por exemplo:

1. O settlement é inserido com sucesso;
2. O update do recebível falha;
3. O recebível continua como disponível;
4. Uma nova tentativa pode gerar outra liquidação.

Isso compromete a integridade financeira dos dados.

### Correção proposta

Executar a criação da liquidação e a atualização do recebível dentro de uma única transação.

Em caso de falha, toda a operação deve sofrer rollback.

---

## 3. Exceção engolida

### Problema

O código captura qualquer exceção e não trata o erro:

```typescript
catch (e) {
  // se falhar aqui, o insert já rodou, então segue o jogo
}
```

### Impacto

O sistema pode apresentar uma operação como concluída mesmo quando uma das operações no banco falhou.

Além disso, o erro não fica disponível de forma adequada para diagnóstico.

### Correção proposta

A exceção deve ser tratada ou propagada para um tratamento global da aplicação.

Em caso de falha na liquidação, a API deve retornar um código HTTP adequado e uma mensagem de erro.

---

## 4. Retorno HTTP 200 em caso de erro

### Problema

Independentemente de uma possível falha no banco, o endpoint termina retornando:

```typescript
res.status(200).json({
  ok: true,
  amount: finalAmount.toFixed(2)
});
```

### Impacto

O consumidor da API recebe a impressão de que a liquidação foi realizada com sucesso mesmo quando ela pode ter falhado.

Isso dificulta o tratamento de erros pelos clientes e pode gerar inconsistências financeiras.

### Correção proposta

Utilizar códigos HTTP semânticos.

Exemplos:

* `201 Created` para uma liquidação criada com sucesso;
* `400 Bad Request` para entrada inválida;
* `404 Not Found` para recebível inexistente;
* `500 Internal Server Error` ou tratamento equivalente para falhas inesperadas.

---

## 5. Cálculo financeiro utilizando `number`

### Problema

O cálculo utiliza valores JavaScript do tipo `number`:

```typescript
const BASE_RATE = 1.0;
```

e:

```typescript
const presentValue =
  receivable.face_value /
  Math.pow(1 + BASE_RATE + spread, receivable.term);
```

### Impacto

Valores monetários podem sofrer problemas de precisão por utilizar ponto flutuante binário.

Em um sistema financeiro, pequenas diferenças podem alterar o resultado final.

### Correção proposta

Utilizar um tipo apropriado para cálculos monetários e taxas, como `BigDecimal` no backend Java.

O cálculo deve manter precisão intermediária e aplicar a política de arredondamento definida pela especificação.

---

## 6. Taxas representadas de forma incorreta

### Problema

O código define:

```typescript
const BASE_RATE = 1.0;
```

e:

```typescript
const spread = receivable.type === "DUPLICATA" ? 1.5 : 2.5;
```

Porém, as taxas do domínio são percentuais:

* Taxa base: 1,0% a.m.;
* Duplicata: 1,5% a.m.;
* Cheque: 2,5% a.m.

Na fórmula, esses valores precisam ser representados de acordo com sua natureza percentual.

### Impacto

Utilizar `1.0`, `1.5` e `2.5` diretamente altera completamente o resultado do cálculo.

O valor calculado deixa de representar a regra financeira esperada.

### Correção proposta

Representar as taxas corretamente, por exemplo:

```text
1,0% = 0,01
1,5% = 0,015
2,5% = 0,025
```

E aplicar a fórmula:

```text
PV = Face Value / (1 + Base Rate + Spread)^Term
```

---

## 7. Arredondamento e conversão cambial

### Problema

O código utiliza:

```typescript
finalAmount = presentValue / rate;
```

e somente depois:

```typescript
finalAmount.toFixed(2)
```

Não existe uma política explícita de precisão e arredondamento durante o cálculo.

### Impacto

O resultado pode divergir dos valores esperados nos golden cases.

Em operações cross-currency, a ordem em que o arredondamento acontece é importante.

### Correção proposta

Manter precisão intermediária no cálculo e aplicar a política definida na especificação.

No caso cross-currency:

1. Calcular o valor presente em BRL;
2. Arredondar o valor em BRL para 2 casas com `HALF_EVEN`;
3. Converter para USD;
4. Arredondar o valor convertido para 2 casas.

---

## 8. Falta de idempotência

### Problema

O endpoint não verifica se o recebível já possui uma liquidação antes de criar um novo settlement.

O código simplesmente executa:

```typescript
INSERT INTO settlements ...
```

### Impacto

Uma repetição da mesma requisição pode gerar duas liquidações para o mesmo recebível.

Isso pode ocorrer por retry de rede, duplo clique ou reprocessamento da mensagem.

O impacto é diretamente financeiro.

### Correção proposta

Verificar se o recebível já está liquidado antes de criar uma nova liquidação e retornar a liquidação existente.

Além da validação na aplicação, deve existir uma restrição de unicidade no banco para garantir a integridade dos dados.

---

## 9. Falta de validação de entrada

### Problema

O código recebe diretamente:

```typescript
const { receivableId, currency } = req.body;
```

sem validar os valores.

### Impacto

Entradas inválidas podem causar erros inesperados ou resultados incorretos.

Exemplos:

* `receivableId` inexistente;
* moeda inválida;
* valores ausentes;
* tipos inesperados.

### Correção proposta

Validar os dados de entrada antes de executar as regras de negócio.

Também deve ser validada a existência do recebível antes de acessar:

```typescript
receivable.type
```

---

## 10. Falta de tratamento para recebível inexistente

### Problema

O código assume que:

```typescript
const receivable = await db.queryOne(...)
```

sempre retornará um recebível.

Depois acessa:

```typescript
receivable.type
```

sem verificar se o resultado existe.

### Impacto

Um recebível inexistente pode gerar erro inesperado durante a execução.

O cliente não recebe uma resposta clara informando que o recurso não foi encontrado.

### Correção proposta

Verificar a existência do recebível e retornar `404 Not Found` quando ele não existir.

---

## Conclusão

O endpoint possui problemas importantes relacionados a segurança, integridade transacional, precisão financeira, tratamento de erros e idempotência.

Os pontos de maior impacto para uma operação financeira são:

1. SQL Injection;
2. Falta de transação;
3. Falta de idempotência;
4. Cálculo financeiro incorreto;
5. Tratamento inadequado de exceções e códigos HTTP.

A correção desses pontos deve priorizar a segurança, a consistência dos dados e a precisão dos valores financeiros.
