# Pirates of MCA — diálogos exclusivos (v1.0)

## Conteúdo

- **216 falas avulsas de piratas**: três temperamentos (`friendly`, `gruff`, `hostile`), 12 contextos, 6 variações por combinação.
- **9 cenas ramificadas**, cada uma com 3 respostas do jogador e 3 reações do pirata.
- **279 chaves de tradução por idioma** em `assets/piratesmca/lang/en_us.json` e `pt_br.json`.
- `dialogues_bilingual.csv`: editor para todas as falas, prompts, escolhas e reações.
- `spec/pool.json`: catálogo **customizado do Pirates of MCA** para seleção de falas avulsas por estado.
- `spec/scenes.json`: índices das cenas, gatilhos e feedbacks.
- `data/piratesmca/dialogues/*.json`: **9 nós no formato de diálogos MCA Reborn**, sem injeção em `main.json`.

## Regra crítica: exclusividade dos piratas

**Não** adicionar diretamente todas as opções a `data/piratesmca/dialogues/main.json` sem filtro: o MCA mescla `main` e ela se tornaria uma opção para villagers comuns. O Java do Pirates of MCA deve permitir a interação/cena apenas se o alvo for um pirata (profissão, componente de identidade ou entidade do próprio mod), além de selecionar o temperamento.

O catálogo `spec/pool.json` **não é** um tipo de datapack nativo reconhecido pelo MCA; seu carregamento e sorteio dependem de código do mod. Os 9 nós MCA são conteúdo estruturado, mas não ficam acessíveis até que seu mod faça o encaminhamento a eles.

## Integração sugerida

1. Detecte `isPirate(villager)` com um marcador persistente do Pirates of MCA (não usar apenas o traço de personalidade do MCA).
2. Defina um temperamento pirata (`friendly`, `gruff`, `hostile`) por NPC, persistido em NBT/Attachment; mapeie personalidade MCA como fallback somente.
3. Em eventos do personagem (`interact`, `chat`, `trade`, `gift`, `danger`, `farewell` etc.), filtre o pool por `pirateOnly`, `temperament` e `trigger`.
4. Evite as últimas 4 linhas do NPC, para que a fala não fique repetitiva. Use fallback no mesmo temperamento quando um contexto específico não estiver disponível.
5. Para as cenas, injete um caminho de entrada em sua GUI ou fluxo MCA **somente para piratas**. O arquivo do nó já contém `answers`/`results` e `say` com chaves traduzíveis, mas os efeitos `positive`/`negative` são apenas metadados de design e **não aplicam corações automaticamente**.
6. Valide as API, o carregador de idiomas e as ações na versão real do MCA Reborn **8.x / Minecraft 26.2** antes de prometer compatibilidade. O formato de nó foi baseado na documentação do motor MCA e não foi testado dentro de um jogo nessa versão.

## Proposta de roteamento (não obrigatório)

| Temperamento | Características | Aplica efeitos especiais? |
|---|---|---|
| `friendly` | caloroso, explorador, protetor da tripulação | escolha de ajuda pode aumentar confiança |
| `gruff` | ríspido, exigente, fiel a quem trabalha | desrespeito reduz respeito; ajuda aumenta |
| `hostile` | desconfiado, intimidador, passível de confrontos | provocações elevam tensão; não inicie ataque apenas por selecionar fala |

Sugestão para o sistema de rancor/motim existente: cada resposta com `outcome` `positive`, `neutral` ou `negative` pode alterar o medidor de respeito/tensão, mas a decisão de iniciar briga deve continuar com o sistema do mod (e suas regras de segurança de combate). Não vincule hostilidade diretamente a dano sem checar aliança e provocação.

## Estrutura de tradução MCA

- MCA usa `dialogue.<question>` para a pergunta e `dialogue.<question>.<answer>` para a opção do jogador.
- A ação `say` referencia uma chave traduzida sob `dialogue.<sayId>`; os nove arquivos seguem esse padrão.
- A pasta `assets/piratesmca/lang/` pode ser incorporada ao JAR. Verifique eventuais convenções específicas de fala/overlays na versão do MCA instalada.

## Testes sugeridos

- Pirata friendly, gruff, hostile: fala correspondente ao próprio temperamento.
- Villager MCA comum: nenhuma opção ou linha do Pirates of MCA.
- Inglês e pt-BR: todas as 279 chaves aparecem traduzidas; nenhum raw key.
- Cada uma das 9 cenas: 3 opções acessíveis, resposta coerente em ambas as línguas.
- NPCs de mesmo temperamento: 4 interações seguidas sem repetição imediata, exceto pool esgotado.
- Eventos de combate, trocas e presentes preservam o comportamento MCA (sem triggers indevidos).

## Fonte de referência para o formato de diálogo

MCA Reborn — wiki de Dialogues: https://github.com/Luke100000/minecraft-comes-alive/wiki/Dialogues
Referência técnica do motor MCA (MCA: Conversations): https://github.com/otectus/MCAConversations/blob/main/DATAPACK.md
