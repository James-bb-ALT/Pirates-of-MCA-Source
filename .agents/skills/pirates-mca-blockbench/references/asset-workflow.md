# Workflow repetível

1. Selecionar asset do `definitions/asset_catalog.json`.
2. Abrir os concepts visuais em `art/concepts/` e escolher silhueta e variante.
3. Confirmar target de renderização do MCA e modelo-base real.
4. Criar fonte editável `.bbmodel`, PNG e um registro com ID estável.
5. Exportar para `exports/emf/` **ou** `exports/fabric_renderer/` segundo validação.
6. Conectar ao preset de NPC ou item, sem substituir a pele MCA.
7. Atualizar catálogo: `status=ready` apenas quando os paths existentes e a validação passarem.
8. `python scripts/pmca/validar_assets.py --root .`.
9. Teste in-game e relatório `reports/<id>.md`.
10. Só depois iniciar próximo asset.

## Prioridade
- Lote 0: auditoria MCA/EMF, tricórnio de referência.
- Lote 1: três roupas base, tricórnio, tapa-olho, faixa e debug.
- Lote 2: roupas restantes, skins, próteses, bolsas, bainhas e lanternas.
- Lote 3: 10 armaduras, modularidade e presets.
- Lote 4: QA performance, clipping, migração e revisão do modpack.

## Nomes
- usar snake_case, `piratesmca:identificador`, sem espaços em paths.
- o nome do asset não deve depender de gênero, salvo incompatibilidade real de UV/modelo.
- manter labels traduzidas separadas dos IDs técnicos.
