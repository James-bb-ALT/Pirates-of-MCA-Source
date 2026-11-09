# Armas handheld — 11.1.1-alpha.17

PASS nos cenários executados. Correção Minor conforme a classificação do usuário.

## Correção

As dez armas possuíam sprite de inventário, mas o seletor de display_context usava uma malha voxel quando seguradas. Agora cada item resolve diretamente seu modelo `minecraft:item/handheld` em todos os contextos, com o PNG original em layer0. O Minecraft gera frente, costas e bordas com profundidade de um pixel; não há cubos autorais nem transformações próprias de arma.

Export reproduzível: `python scripts/pmca/export_atelier_weapons.py`. A reimportação do Atelier aplica esse export por último. Ganchos e pernas continuam 3D.

Preservação comprovada por comparação binária com o JAR 11.1.0-alpha.16: mudaram somente os vinte JSONs de definição/modelo das dez armas e fabric.mod.json. Todas as classes de produção, PNGs, armaduras, próteses, atributos, durabilidade, loot, tags e perfis de Better Combat permanecem idênticos. Detalhes em weapon-handheld/preservation.json.

## Testes

| Caso | Resultado | Evidência |
|---|---|---|
| WH-01: dez armas sem fallback voxel | PASS | Duas regressões Python; onze testes Python totais aprovados. |
| WH-02: defesa dos contratos existentes | PASS | 35 testes JVM e validadores de inventário/Atelier aprovados; nenhuma classe de produção mudou. |
| WH-03: modelos carregados | PASS | Dez armas em GUI, primeira/terceira pessoa esquerda/direita, chão e moldura: 70 resoluções não vazias. Espessura GUI medida no modelo efetivamente carregado: 0,0625 bloco em cada arma. |
| WH-04: classificação Punchy/Better Combat | PASS | Punchy ativo, bridge disponível/configurada e isWeaponStack=true para todas as dez armas. |
| WH-05: ataques reais em primeira pessoa | PASS | Entrada de ataque, swing de Better Combat em andamento e incremento dos contadores de ataque e reprodução visual do Punchy para cada arma. Dez séries de três quadros, mais repouso e terceira pessoa: cinquenta capturas novas. |
| WH-06: ganchos/armaduras preservados | PASS binário | Nenhum arquivo desses recursos mudou em relação à versão anterior. Não houve nova matriz gráfica completa de roupas nesta correção. |

Ambiente: Minecraft26.2, Java25, Fabric Loader0.20.1, Loom1.17.21, MCA8.1.11+26.2, Fish26.2.2.0, Punchy3.1b, Better Combat3.2.2, EMF3.3.11, ETF7.2.5, EMF Compat Core2.3.2/Better Combat2.1.0, Fresh Animations1.11.2, All Extensions1.10.1 e FA+Player1.1. Packs selecionados e SHA256 do JAR constam no log final.

Comando: `gradlew.bat runProductionClientTest -PweaponHandheldReview -PcompatMods=qa/fixtures/weapon-punchy/mods --daemon`.

O teste exige `bettercombatCompat=true` no Punchy e o pack embutido `punchy:punchy` ativo, além dos três packs Fresh. A configuração pessoal do usuário já tinha essa integração ativa e foi apenas consultada; nenhuma alteração foi feita no perfil.

## Falhas de preparação preservadas

As primeiras execuções não são registradas como PASS: o reset de opções do Fabric GameTest encontrou tecla nula de registro tardio; a fixture herdava uma lista sem o pack Punchy; F1 ocultava as próprias mãos em primeira pessoa; o Punchy da fixture nova usava o padrão bettercombatCompat=false. A fixture foi corrigida. Um mixin exclusivo do JAR de testes deixa teclas tardias sem vínculo no reset; esse código nunca entra no addon de produção. Ataque/uso vanilla mantêm os vínculos normais. Os logs de tentativas anteriores acompanham o log aprovado.

Limites: ataques capturados com mão principal direita; contextos esquerdos foram resolvidos/baked, sem nova série de ataque canhoto. Não certifica todas as configurações de Punchy nem o modpack pessoal completo. Não repete os cenários sociais, persistência, profiling ou catálogo de armaduras da entrega anterior.

## Instalação e fontes

Substitua somente o JAR antigo pelo 11.1.1-alpha.17 e reinicie o jogo. Sources/gametest não são mods para instalar. Não é necessário adicionar um resource pack de armas: os modelos estão embutidos.

O ZIP source-delta contém os arquivos alterados e novos, testes e scripts; aplique sobre o workspace completo 11.1.0-alpha.16 já entregue. O sources.jar contém os fontes/recursos principais atualizados. Logs, capturas e manifests acompanham o pacote de evidências. Os JARs opcionais de outros autores não são redistribuídos.

Commit/tag locais; não há remoto GitHub configurado para push ou release remota.
