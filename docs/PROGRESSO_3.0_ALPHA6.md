# Etapa 3.0.0-alpha.6 — encerrada como teste jogável parcial

Encerramento solicitado para preservar a cota. Resultado atual: 31 métodos JUnit,
35 GameTests e smoke de cliente com 48 NPCs PASS. SavedData/codec e recuperação
também passaram. O relatório final desta etapa é `RELATORIO_TESTE_JOGAVEL_3.0.md`.

O checkpoint **2.1.0-alpha.6** está preservado em `delivery/2.1.0-alpha.6`, com
fontes, binário, relatórios e hashes próprios. Seu relatório não certifica o código
posterior descrito aqui. Nova etapa de release conforme a classificação do usuário.

## IA, ordens e hierarquia

- Diretivas persistem dimensão, ponto e UUID do alvo; encerrar/dispensar limpa a
  diretiva. ATTACK aceita monstro hostil mirado ou UUID explícito, validado no
  servidor, sem substituir por outro monstro quando o alvo some.
- HOLD registra a posição do tripulante; GUARD/RALLY registram ponto do capitão.
  FOLLOW pode seguir capitão NPC; RETREAT limpa perseguição. Seleção NEAREST
  usa mira de seis blocos, NEARBY usa esfera de doze, ALL usa somente membros
  carregados da mesma crew/dimensão, sem forçar chunks.
- Imediato assume rotina quando o capitão está ausente; mantém FIRST_MATE e
  `actingCaptain`, preservando o capitão definitivo. A disponibilidade global do
  capitão evita alternar autoridade por NPCs em dimensões diferentes.
- Provas: `orders-first-validation.log`, `orders-navigation-validation.log` e
  execuções sociais posteriores. Teste com IA ativa segue capitão NPC e mantém HOLD.
  Fixtures de navegação agora carregam explicitamente os chunks que extrapolam
  a estrutura padrão pequena do GameTest.

## Eventos, testemunhas e rumores

- Eventos estruturados têm UUID, crew, tick, tipo, ator, alvo, motivo, severidade,
  testemunhas e consequências idempotentes; últimos 128 por crew.
- Rumor exige emissor informado e encontro vivo/carregado com visão; destinatário
  recebe impacto reduzido. Repetir a notícia, inclusive após load, não repete efeito.
- Rivalidade passa a exigir relação negativa ou disputa por ItemEntity real.
  O perfil BRUISER isolado não cria causa de briga. A rolagem de 15% fica ligada
  a um evento PROVOCATION e não é repetida por tick ou após reload.
- Falha reproduzida: retorno de dano fatal criava CAPTAIN_ASSAULT depois de MURDER,
  adicionando outra penalidade e fazendo circular a agressão menor antes da morte.
  Diagnóstico: `social-witness-diagnostic.log` mostra visão/distância válidas e
  ressentimento 37/1. A correção impede agressão sobre registro/entidade morta.
  `social-witness-callback-fix.log` PASS, incluindo morte real, testemunha próxima,
  membro a 32 blocos desinformado e posterior rumor presencial sem duplicação.
- A fixture aguarda outro tick após teleporte, antes de consultar o cache de visão.

## Ainda em trabalho

Bandeira, interfaces e tesouraria; diplomacia; validação completa de integração,
migração histórica e profiling. Cadência, fases, rendição e saída segura têm
regressões novas, mas não constituem certificação de todo o motim ponta a ponta.
A matriz atual identifica os cenários efetivamente cobertos e os não executados.
