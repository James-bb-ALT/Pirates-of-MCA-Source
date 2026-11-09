# Faixa vermelha — 9.0.1-alpha.12

SASH-FIT-01 corrigido no caso reproduzido. A faixa agora segue o centro dos
pivôs reais do quadril e mantém o yaw do torso. A transição de acordar não a
arrasta para dentro das pernas. Malha, textura e dados persistidos preservados.

Ver [causa-raiz, regressão e evidências](sash_fit_9.0.1.md).
O relatório da candidata bloqueada 9.0.0 foi preservado em
[delivery/9.0.0-alpha.12](sash-fit/before/sash_red_9.0.0.md).
A hipótese anterior de limitação do corpo feminino não se confirmou.

Fonte: models/blockbench/torso/sash_red.bbmodel. PNG: textures/wearables/waist/sash_red.png.
Export: exports/fabric_renderer/sash_red.json. Preset: definitions/presets/sash_mvp.json.
Slot lógico cintura mapeado para CHEST nativo neste MVP, sem defesa ou sobreposição
com outro peitoral. Tricórnio/tapa-olho em HEAD são independentes.

```text
/give @s piratesmca:sash_red
/pirates preset nearest sash_red
/pirates preset nearest sash_clear
```

35 JUnit, 64 GameTests, sete testes Python e clientes OFF/stack completo EMF PASS
no escopo do relatório. Shaders, modpack completo, todos os corpos/avatares e
profiling continuam sem certificação. Catálogo permanece exported/animation_safe=false.
