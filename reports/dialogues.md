# Diálogos — 12.0.0-alpha.18 — 2026-11-05

## Uso e escopo

Instale somente o JAR principal em mods, substituindo o Pirates of MCA anterior. Requer Minecraft 26.2, Java 25, Fabric Loader 0.20.1, Fabric API, MCA 8.1.11+26.2 e Fish of Thieves 26.2.2.0.
Shift + clique direito com mão principal vazia em um pirata ou /pirates talk abre assuntos. Avisos automáticos oferecem Responder sem abrir uma janela à força. O idioma acompanha en_us/pt_br do cliente.

Importadas 216 falas, nove cenas e 27 escolhas: 279 traduções originais por idioma. Somente adultos vivos marcados como piratas e com profissão Pirata participam. Os nós MCA fornecidos ficam arquivados, fora do roteador global. Preserva interação, comércio e presentes MCA.

## Execução

| Verificação | Resultado | Evidência |
|---|---|---|
| Build Java 25 / Loom com dependências reais | PASS | dialogues-final.log, BUILD SUCCESSFUL |
| JUnit: 44 testes, zero falhas | PASS | XML de build/test-results/test |
| Validadores de assets e testes Python | PASS | tarefas validatePirateAssets, validateTricorneExport e validateAtelierAssets |
| Nove cenas em cliente Minecraft real | PASS | DLG_AUTO_CONSPIRACY_PASS e oito DLG_SCENE_PASS |
| EN/PT, isolamento de NPC comum | PASS | DLG_LOCALE_PASS e DLG_ISOLATION_PASS |
| Presente e loja nativos MCA | PASS | DLG_NATIVE_GIFT_PASS / DLG_NATIVE_TRADE_PASS |
| Resposta repetida, token falso e distância | PASS | DLG_AUTO_CONSPIRACY_PASS / DLG_PERSISTENCE_RANGE_PASS |
| Histórico/cooldown via save/load nativo da entidade | PASS | DLG_PERSISTENCE_RANGE_PASS e testes unitários |
| Preservação de 15677 assets anteriores PNG/modelos/equipment | PASS | comparação binária com alpha.17; traduções antigas preservadas |
| Reiniciar jogo, descarregar chunks e mudar dimensão com conversa ativa | BLOCKED | não executado nesta rodada |
| Modpack FO completo e multiplayer com clientes de idiomas distintos | BLOCKED | não executado nesta rodada |
| Profiling de 4/12/24/48 NPCs | BLOCKED | não executado nesta rodada |

## Gatilhos

| Cena | Condição real |
|---|---|
| mutiny_whisper | fase de conspiração/ultimato/rebelião, conspirador vivo presente e observador leal |
| captain_test | tripulação sem capitão |
| missing_crate | jogador quebra recipiente perto da bandeira da tripulação |
| damaged_hull | dano real em barco ocupado por pirata ou capitão da tripulação |
| share_the_find | tesouro na mão ou presente aceito pelo MCA |
| voyage_invitation | ordem FOLLOW e embarcação |
| lost_sailor | colega vivo carregado, separado mais de 24 blocos e dentro da água |
| deck_trespass | jogador estrangeiro próximo da bandeira |
| rival_flag | outra bandeira de tripulação vinculada e fisicamente próxima |

Doze contextos: cumprimento, conversa, mar, tesouro, comércio, capitão, tripulação, lealdade, rumor, perigo, presente e despedida. Ordens, sucessão e conflitos sociais usam eventos reais da tripulação. Falas específicas de tempestade, casco, rivalidade e conspiração exigem seus sinais. Avisos urgentes de conspiração podem usar o texto fornecido independentemente do temperamento; as demais cenas respeitam seu tom.

## Persistência e limites

Memória limitada às últimas quatro falas e quatro eventos, com cooldown persistido nas tags nativas. Sessões são temporárias, vinculadas a jogador/NPC/tripulação e revalidam dimensão, alcance, validade e contexto. Respostas não concedem capitania nem iniciam combate; efeitos sociais têm limite de uma aplicação por 60 segundos. Um NPC descarregado não é considerado perdido ou morto.

Uma escolha por cena foi executada em jogo; as 27 alternativas foram verificadas estruturalmente, não todas clicadas. Os testes preparam fixtures e reiniciam o cooldown de fala entre cenas, sem alterar o cooldown de produção. Textos narrativos do pack não implementam missões, recompensas, resgate automático ou reparo físico de barcos. Sem mudança no schema da tripulação.

Houve tentativas anteriores com falha no teste de codificação PT e na fixture de marinheiro fora de chunk carregado; corrigidas e reexecutadas. Uma execução intermediária foi cancelada para evitar testar compilação anterior ao hook de dano. A primeira captura visual ocorreu durante o overlay de reload; as capturas foram refeitas aguardando o overlay desaparecer. Logs finais e de recaptura acompanham as evidências.

## Reprodução

gradlew.bat jar sourcesJar test validatePirateAssets validateTricorneExport validateAtelierAssets runProductionClientTest -PdialogueReview --daemon

O teste usa run/dialogue-test isolado. Não usa stubs. O importador scripts/pmca/import_dialogues.py recebe --zip; não executa bytecode do ZIP nem ativa cenas globalmente no MCA.
