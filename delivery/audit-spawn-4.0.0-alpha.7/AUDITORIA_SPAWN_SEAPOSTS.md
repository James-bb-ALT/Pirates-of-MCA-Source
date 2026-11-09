# Verificação de geração — seaposts Fish of Thieves

Data: 2026-11-03. Versão avaliada: **4.0.0-alpha.7**.
Pedido: verificar piratas nos seaposts e villagers comuns nascendo como piratas.

## Resultado

**PASS no cenário automatizado de servidor. Não reproduzida conversão indevida.**
O produto não precisou de alteração; foram acrescentadas três regressões reais.
O JAR da entrega 4.0 permanece inalterado:
`cb0f1413ee77409dbe65232aa6bab2f44502ebc89c29d33254c86d98e682bec3`.

| ID desta auditoria | Contrato | Cenário executado | Resultado |
|---|---|---|---|
| SPAWN-SEAPOST-01 | TC-SPAWN-10; TC-BOOT-05 | Colocar template original Fish pela classe SeapostPiece.postProcess, executar os marcadores e a fila de conversão MCA | PASS: 2 piratas adultos, mesma crew, capitão definido, nomes válidos, pele/roupas presentes, roupa piratesmca e gênero consistente com a tag |
| SPAWN-ISOLATION-01 | TC-AI-03, verificação parcial | 12 villagers vanilla, profissões fisherman/none, convertidos pelo MCA | PASS: 8 adultos e 4 crianças; zero profissões/tags/roupas de pirata; zero registros de crew |
| SPAWN-ISOLATION-02 | TC-AI-03, verificação parcial | 24 villagers MCA de ambos os tipos, fisherman/none, ao lado de um pirata de controle | PASS: zero conversões indevidas; 8 crianças preservadas; profissões e roupas comuns preservadas |

**44 GameTests obrigatórios passaram**, incluindo os 41 anteriores. A rodada
terminou normalmente: `BUILD SUCCESSFUL in 48s`. Evidência principal:
`qa/results/spawn-isolation-validation.log`, marcadores `QA_REAL_SEAPOST`,
`QA_SEAPOST_NPC`, `QA_ORDINARY_CONVERSION` e `QA_NEARBY_ISOLATION`.
Os novos métodos estão em `SpawnIsolationGameTests.java`, registrados no testmod.

## O que controla a conversão

`SeapostPiratesMixin` marca somente os dois villagers criados nas ramificações
fisherman/leatherworker do Fish. A fila SpawnQueue do MCA copia essas tags para
os NPCs substitutos. `PirateBehavior.tick` exige `pmca_seapost` antes de alterar
idade, profissão, roupa ou perfil. A proximidade de um pirata não concede a tag.
A profissão Pirata tem heldJobSite `PoiType.NONE`; não usa um bloco profissional
normal para transformar moradores de aldeia em piratas.

O teste anterior `seapostMixinTargets` apenas carregava a classe. Ele não provava
que a estrutura produzia piratas. A nova regressão atravessa a colocação da
estrutura, os marcadores, a conversão MCA e o tick do addon.

## Reprodução

No projeto, com Java 25 e as dependências fornecidas:

```powershell
.\gradlew.bat runGameTest --console=plain
```

Stack preservada: MC 26.2, Fabric Loader 0.20.1, MCA 8.1.11+26.2,
Fish 26.2.2.0, API 0.155.2+26.2 e YACL 3.9.7+26.2. Sem stubs.

As tentativas preliminares falharam por montagem da cena: o Fish reposiciona o
template no nível do mar e os chunks externos à pequena estrutura GameTest não
estavam forçados a receber ticks. O teste passou a usar clipping vertical completo,
ler a origem depois da colocação e manter os chunks do seapost em execução,
liberando-os em finally. As regras do produto não foram afrouxadas.

## Limites da conclusão

- O template e o pipeline são reais, colocados em um mundo controlado GameTest.
  Não foi percorrido um oceano com geração natural por seed nesta rodada. Por isso,
  **TC-SPAWN-10 completo continua NOT RUN**, com esta prova parcial registrada.
- Esta rodada é de servidor. Não houve nova inspeção visual no cliente, FO,
  reprodução no save do usuário, nem teste prolongado de navegação nos deques.
- Villagers comuns usaram NoAI para estabilizar as amostras. O cenário completo
  TC-AI-03 contra monstros continua NOT RUN; estes testes comprovam isolamento
  de spawn, idade, profissão, roupa e ledger, não toda a IA.
- Um mundo contaminado por versões antigas, configurações próprias ou outros mods
  não foi reproduzido. NPCs já marcados por versões antigas não são desmarcados
  automaticamente, para evitar apagar piratas legítimos.
- Logs da suíte incluem erros deliberados dos testes de recuperação de persistência.
  O resultado dos testes e a saída do processo, não a ausência de ERROR, são a prova.

Não foi criada nova versão binária: a tarefa atual é de verificação e nenhum
comportamento de produção foi alterado. Os pacotes anteriores permanecem imutáveis.
O diretório não possui Git/remoto; não há publicação remota.
