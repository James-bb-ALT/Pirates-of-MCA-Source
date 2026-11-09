# Reconstrução — Fase 2 / geometria

PASS estático: 1091 equipment IDs, 4349 slots, 54098 cubos e 324588 faces. Todos os PNGs existem, UVs dentro dos atlases, chapéus abertos sem caixas atravessando os olhos. Cinco máscaras de caveira são fechadas intencionalmente. Teste: scripts/pmca/validate_wardrobe.py.

1062 conjuntos utilizam os quatro CEMs correspondentes, convertidos em fontes BBModel e exportados em assets/piratesmca/wardrobe/pirateatelier. 29 legados sem fonte CEM receberam adaptação autoral explícita dos volumes, com os atlases originais do próprio equipamento por slot; proveniência e papéis constam nas fontes BBModel. Nenhum atlas original foi repintado.

A camada WardrobeLayer segue root e ModelPart.translateAndRotate do modelo efetivamente usado pelo renderer. WardrobeArmorMixin cancela apenas a peça equipada que tem mesh resolvida; o restante da armadura vanilla permanece. Cache é invalidado por resource reload. Próteses usam corte superior na nova malha, sem redesenhar a armadura vanilla por cima.

PASS cliente real: os dois conjuntos prioritários foram capturados em três vistas no estágio geometry, com o renderer novo. O rosto ainda está parcialmente encoberto pela segunda camada da skin original; a roupa MCA ainda está sobreposta porque a fase de skins não foi aplicada. A máscara de roupa inferior MCA ainda depende da Fase 3. Capturas/log em geometry/.

PENDENTE: capturas em jogo dos 29 legados e matriz completa, A/B EMF, poses dinâmicas, trims autorais, comparação visual por variante. Geometria desenhada não constitui aprovação integral da aparência. A renderização customizada preserva glint, mas ainda não tem UVs autorais de armor trims; não declarar essa compatibilidade pronta.

Fechamento: catálogo completo capturado, binding das 4349 peças confirmado e quatro cenários A/B aprovados. Glint/trim nativos exercitados nas fixtures de jogador. A pendência de revisão artística universal permanece; os parágrafos anteriores registram o estado histórico desta fase.
