# Render targets e formatos (MCA / EMF / Fresh)

## Decisão técnica

| Asset | Rota possível | Risco |
|---|---|---|
| Skin do MCA | textura/skin/genética original | sobrescrever rosto/cabelo ou randomizar no relog |
| Roupa MCA | overlay/textura com UV documentado | pintar rosto opaco, clipping com 3D |
| Tricórnio, acessórios | Fabric render layer ligada ao modelo; opcional CEM se entidade for reconhecida | falta de pivô/render target |
| Gancho/perna de pau | render layer+ocultação seletiva; potencial CEM com partes de entidade verificadas | duplicar mãos/pernas, desincronizar animação |
| Armadura | armadura/item e renderer correspondente; EMF custom armor CEM apenas para alvos suportados | slots incompatíveis, substituição global de armaduras |
| Item no inventário | Java Block/Item model | não acompanha esqueleto NPC automaticamente |

- EMF implementa suporte CEM de OptiFine (`.jem`/`.jpm`), incluindo versões para alguns modelos de armadura bípede e modelos exportados a partir do jogo; não garante que seu renderer MCA seja coberto.
- Em `/assets/minecraft/optifine/cem/` os `.jem` substituem modelos específicos e podem conflitar com Fresh Animations. Não sobrescreva arquivos Fresh sem objetivo explícito, diff e teste.
- O plugin CEM Template Loader/formatos OptiFine Entity são úteis **somente depois** de confirmar o modelo alvo.
- `bbmodel` é JSON interno editável com mudanças por versão. Blockbench 5.0 moveu grupos/outliner; não confundir com modelo Java Block ou CEM exportado.
- Sem runtime, a conclusão é `INCONCLUSIVO`, não `COMPATIVEL`.

## Links oficiais
- https://blockbench.net/wiki/docs/bbmodel/
- https://blockbench.net/wiki/blockbench/formats/
- https://blockbench.net/wiki/docs/plugin/
- https://github.com/Traben-0/Entity_Model_Features/blob/master/FEATURES.md
- https://optifine.readthedocs.io/cem.html
- https://modrinth.com/project/entity-model-features
