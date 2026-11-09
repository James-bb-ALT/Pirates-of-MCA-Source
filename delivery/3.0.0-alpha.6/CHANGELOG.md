# Changelog

## 3.0.0-alpha.6 — 2026-11-03 — teste jogável parcial

Nova etapa de release segundo a classificação do usuário: implementação dos
requisitos funcionais pendentes. O checkpoint 2.1.0-alpha.6 permanece preservado.

- Diretivas de ordens com alvo e âncora persistentes; seleção individual, próxima
  ou de todos os membros carregados da própria crew.
- Imediato assume ordens rotineiras quando o capitão não está disponível, mantendo
  o cargo de imediato e a capitania definitiva original.
- SavedData/codec nativo, importação preservando Properties, escrita atômica e recuperação.
- Eventos com IDs e consequências persistentes; rumores por encontros e provocação causal.
- Rendição segura, fases/cadência de motim e retirada de feridos com destino do Brain.
- 31 métodos JUnit, 35 GameTests e cliente de produção com 48 NPCs passaram.
  Funcionalidades restantes, FO e profiling não foram certificados; ver relatório atual.

## 2.1.0-alpha.6 — 2026-11-03 — auditoria parcial

Classificação do usuário: **Major**, por mudanças amplas de comportamento em combate,
persistência e hierarquia. Não é uma declaração de conclusão da especificação v2.0.

- Build Gradle 9.5.1 / Loom 1.17.21 / Java 25 com Minecraft 26.2 real,
  Loader 0.20.1, dependências fornecidas verificadas por SHA256 e YACL obrigatório.
  Scripts legados de release agora encaminham ao build real; stubs não validam release.
- Apoio ao motim exclui capitão NPC, mortos e crianças. Conspiração começa antes
  do limiar necessário para ultimato; relógio do ultimato pausa com capitão ausente.
- Oficiais não acumulam posições; transferência, morte e promoção limpam o cargo.
  Sucessão determinística considera relações e vantagem institucional do imediato.
- Candidatura de sobrevivência exige vaga e apoio de metade dos adultos da crew;
  não substitui um capitão definitivo. `force` e `debug` exigem OP.
- Brigas limitam o golpe antes do dano a 25% da vida; fogo e efeitos nocivos da
  própria arma são neutralizados no contexto do duelo. Dano externo continua letal.
  Guarda não transforma disputa comum em fogo amigo; contramestre separa ambos.
- Estado de runtime separado por servidor e limpo no unload/stop. Morte ambiental
  retira NPC do roster e dispara sucessão; capitão descarregado não é tratado como morto.
- Reparo visual não rerandomiza genética válida, não sobrescreve roupa MCA bloqueada,
  nem reaplica personalidade/humor a cada segundo. Relações MCA existentes são preservadas.
  Roupas piratas ficam restritas à profissão própria, sem contaminar pescadores comuns.
- Persistência grava por arquivo temporário com flush, substituição atômica quando
  suportada e backups. Falha de escrita não marca estado como salvo; schemas e
  referências inconsistentes são rejeitados preservando o original.
- Testes JUnit, regressões originais, GameTests reais de servidor e cliente,
  evidências visuais e fixture FO oficial com verificação de hashes.

As funcionalidades pendentes e cenários não executados estão no relatório de execução.
Os documentos originais e o JAR Alpha 5 permanecem como histórico.
