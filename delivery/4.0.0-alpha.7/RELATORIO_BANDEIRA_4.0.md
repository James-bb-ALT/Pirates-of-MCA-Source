# Bandeira e fundação — 4.0.0-alpha.7 — 2026-11-03

Entrega parcial da especificação v2.0. Os contratos originais não foram alterados.
Nova etapa de release conforme a classificação do usuário.

## Execução

| Validação | Resultado | Evidência |
|---|---|---|
| TC-BOOT-01: clean build real Java 25/Loom/MC 26.2 | PASS | qa/results/flag-final-build.log |
| 35 métodos JUnit | PASS | qa/results/flag-junit; mesmo log |
| 41 GameTests obrigatórios (40 addon, 1 Fabric) | PASS | mesmo log |
| TC-BOOT-02: cliente de produção, mundo integrado | PASS | qa/results/flag-client-validation.log |
| Fundação pelo botão real da bandeira; dois voluntários | PASS | QA_FLAG_DIALOG_FOUNDED; capturas FLAG |
| Ordem HOLD pelo menu | PASS | asserção de evento; aceitação continua contextual |
| 48 adultos, identidades/texturas e reload de recursos | PASS | mesmo log; qa/results/flag-client-evidence |
| Troca de dimensão, unload confirmado, fechar/reabrir servidor integrado | PASS | mesmo log, checkpoints de identidade |
| Bandeira e vínculo após reabrir o mundo | PASS | asserções do cliente |
| Reinício de todo o processo Java nesta versão | NOT RUN | Não inferido da reabertura integrada |
| TC-CREW-08 completo e multiplayer/desconexão | NOT RUN | Sete botões não certificam todos os cenários de ordens |
| FO 14.1.1 / compatibilidade completa nesta versão | NOT RUN | Resultado histórico não transferido |
| Profiling 4/12/24/48 por dez minutos | NOT RUN | Smoke de 48 usa NoAI; não mede custo da IA |
| Mundos históricos Alpha 1–5 | BLOCKED | Saves históricos não fornecidos |

Stack: Java 25, Minecraft 26.2, Loader 0.20.1, Loom 1.17.21, Gradle 9.5.1,
MCA 8.1.11+26.2, Fish 26.2.2.0, Fabric API 0.155.2+26.2 e YACL 3.9.7+26.2.
Compilação e validação usam os JARs reais; nenhum stub valida esta entrega.
JAR testado no cliente e entregue: `cb0f1413ee77409dbe65232aa6bab2f44502ebc89c29d33254c86d98e682bec3`.
Novo empacotamento de JAR e sources produziu bytes idênticos; hashes e log em
qa/results/flag-first-hashes.json e flag-reproducible.log. Não é prova de build
offline em outra máquina.

## Implementação e regressões

FLAG-01: bloco, item, receita real, drop e diálogo nativo. Antes faltava a rota
de fundação por bandeira; agora a interação abre o menu e cria a tripulação.
FLAG-02: consentimento, dois adultos livres, autoridade e propriedade exclusiva.
FLAG-03: ações recusadas se distante, com parede ou bandeira removida; comandos
recebidos pela tela são revalidados no servidor.
FLAG-04: regressão de servidor comprova a âncora de RALLY; domínio comprova estado
preservado e duplicatas rejeitadas. GUARD usa a mesma âncora na implementação,
mas a movimentação GUARD nesta âncora não tem teste dedicado. Testes não equivalem
a certificação de navegação prolongada em todas as condições.
FLAG-05: SavedData v3 migra para v4, mantendo o estado e uma cópia
`crews.dat.pre-v4.bak`. O vínculo sobrevive à reabertura do mundo de teste.

Classes: FlagLedgerTest, FlagGameTests, NativePersistenceGameTests e
PirateClientGameTests. A planilha FLAG_TEST_CASES.csv mapeia a extensão aos IDs
TC-BOOT-01/02 e TC-CREW-01/06/08. A matriz original de 62 casos conserva evidências
e limitações históricas; não é uma certificação nova de todos os casos.

Falhas durante o desenvolvimento ficaram nos logs preliminares: cena GameTest com
parede/posição inválida e helper Fabric que reconhece Button, mas não todos os
AbstractButton usados pelo diálogo. A cena e o helper do teste foram corrigidos;
as validações de alcance/visibilidade do produto foram preservadas.

## Limitações

Tesouraria/recompensas/PARDON, diplomacia, Livro de Bordo/HUD e integração completa
dos diálogos MCA permanecem pendentes. ATTACK continua via comando com alvo monstro.
O comando legado captain found permanece sem bandeira para preservar a função e
o contrato de teste anteriores; o consentimento novo é aplicado à rota da bandeira.
Quebrar a bandeira não dissolve a crew; recolocar no mesmo ponto recupera o vínculo.
Cada crew possui uma âncora ativa; vincular outra libera o ponto anterior.
Modelo e textos EN foram inspecionados em tela; PT tem recursos, sem rodada visual PT.
O formato v4 não deve ser aberto por versões antigas: elas rejeitam schema futuro.
Migração sintética não comprova compatibilidade de todos os saves históricos.
FO teve hang intermitente no encerramento de uma versão anterior; sem novo resultado.
O smoke registrou atraso de 13,3 s durante a sequência de viagens/chunks; sem
profiling não é possível atribuir a causa nem certificar desempenho. O perfil de
teste também registrou falhas de autenticação Realms/perfil (HTTP 401) e opção
gráfica de anisotropia inválida. A rodada concluiu, mas o log não está sem erros.
Drop e obtenção pela aba criativa estão implementados, sem teste de interação dedicado.

Nenhum perfil/save pessoal foi alterado. EULA somente nos testes isolados autorizados.
Sem commit/push/tag/release remota: diretório não possui Git nem remoto.
