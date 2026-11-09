# Quality gates por asset

## P0 — Não publicar
- Source `.bbmodel` editável salvo e abre com texturas.
- PNG não faltante, UV consistente e alfa correto onde necessário.
- Renderer realmente carrega o export sem `NoSuchMethodError`, `InvalidMixinException` ou falha de registry.
- Skin/cabelo/rosto/gênero/idade e roupa do MCA preservados no primeiro render.
- NPC continua íntegro depois de chunk unload/reload, save/restart e mudança de dimensão.
- Modelo não duplica corpo/cabeça/mão/perna em Fresh Animations.
- Gancho/perna de pau têm os pivôs corretos e nenhum dano ao vanilla hitbox.
- Entrega informa ambiente, comandos utilizados, screenshots e o resultado PASS/FAIL/BLOCKED.

## P1 — Polimento
- Acessório não atravessa membros durante walk/run/swim/attack/sit/sleep/hurt.
- Compatível com dois gêneros e variações de skin/cabelo do MCA.
- Performance (cubos, texturas, render calls) medida com 4/12/24/48 NPCs.
- Preset estável, seed/UUID persistente e distribuição de variantes documentada.
- Emissivos funcionam somente em variante com suporte demonstrado.

## Caso de teste mínimo por cada asset
1. Spawn de MCA adulto masculino/feminino; verificar corpo e roupa.
2. Item equip e unequip em sobrevivência.
3. Rotação 360° e poses idle/walk/attack/hurt/swim/sit/sleep.
4. Distanciar, descarregar chunk, voltar; sair e reabrir save.
5. EMF/ETF/Fresh em ON e OFF; comparar screenshots.
6. Combinar com chapéu/cabelo/armadura/protese do lado oposto.
7. Render com/sem shader; falhas de profundidade/alpha.
8. Registrar qualquer falha in-game e marcar release BLOQUEADA.
