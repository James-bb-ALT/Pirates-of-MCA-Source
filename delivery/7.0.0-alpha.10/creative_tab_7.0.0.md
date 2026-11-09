# Aba criativa e ovos — 7.0.0-alpha.10

Data: 2026-11-04. Nova etapa de release conforme classificação do usuário.
Minecraft 26.2, Java 25, Fabric Loader 0.20.1 e dependências preservadas.

## Comportamento

Aba **Pirates of MCA**, ícone de tricórnio. Contém bandeira, tricórnio, ovo de
pirata adulto masculino e ovo de pirata adulta. Enumeração do namespace inclui
automaticamente futuros itens registrados, sem duplicar bandeira/tricórnio.
Entradas anteriores em abas vanilla foram preservadas. Ovos também aparecem
na aba vanilla de ovos. Nenhum item de outro mod é adicionado automaticamente.

Os ovos usam entidades MCA reais, EntityData nativo com Age=0, marca de pirata
e gênero explícito. A rotina existente atribui identidade, profissão, roupas,
personalidade e comportamento. Não equipam tricórnio automaticamente, não criam
tripulação artificial e não tornam o jogador capitão. Seguem as regras sociais.
Modelos de ovo referenciam o recurso vanilla de villager; nomes diferenciam gêneros.

```text
/give @s piratesmca:male_pirate_spawn_egg
/give @s piratesmca:female_pirate_spawn_egg
```

## Causa-raiz reproduzida e corrigida

O caminho vanilla SpawnEggItem.spawnOffspringFromSpawnEgg cria um filhote e chama
applyComponentsFromItemStack, sem passar pela aplicação de ENTITY_DATA do spawn
normal. Antes da correção, CREATIVE-03 falhou no tick zero: faltavam a idade adulta
e/ou a marca de pirata. Log: qa/results/creative-tab-before-offspring-fix.log.

PirateSpawnEggMixin intercepta somente os dois ovos do addon nesse método. Mantém
a validação de tipo e usa EntityType.spawn com o ItemStack original, que carrega
ENTITY_DATA. Consome um ovo apenas se criar a entidade. Ovos vanilla e MCA não
são interceptados. CREATIVE-03 testa esse caminho; CREATIVE-05 protege o filhote vanilla.

## Validação

| ID | Estado | Escopo |
|---|---|---|
| CREATIVE-01 | PASS | Aba contém exatamente todos os itens registrados do addon, sem repetição |
| CREATIVE-02 / TC-SPAWN-01/02/03 | PASS | Uso real dos dois ovos: NPC adulto, gênero correto, marca/identidade/profissão |
| CREATIVE-03 | PASS após FAIL reproduzido | Uso em entidade adulta preserva os dados e não deixa criança |
| CREATIVE-04 | PASS | Dispenser acionado por redstone consome ovo e gera um pirata adulto |
| CREATIVE-05 | PASS | Ovo vanilla continua gerando filho comum |
| CREATIVE-06 | PASS | Tela criativa real e dois NPCs no cliente isolado, OFF e stack completo EMF; manifests em creative-tab/off e extensions |

Build: 35 JUnit, 58 GameTests obrigatórios (cinco novos), três testes Python e
validador de assets. Log final em qa/results/creative-tab-build.log.
Os dois clientes terminaram BUILD SUCCESSFUL. A aba estava na página 2 de 2 nos
fixtures; o perfil completo pode ter mais páginas. Navegue pelas setas do topo.
Stack ON: EMF 3.3.11, ETF 7.2.5, EMF Compat Core 2.3.2, EMF Compat Better Combat
2.1.0, Better Combat 3.2.2, Player Animation Library 1.2.8, Cloth Config 26.2.155;
Fresh base 1.11.2, All Extensions 1.10.1 e Player 1.1 ativos, com gate de presença.
Perfil pessoal não modificado. As capturas reais estão em creative-tab/off e
creative-tab/extensions, junto de logs e manifestos SHA256.

JAR testado: `3807430b50953e2ae69773b2862ca98356cba98b4b02aa73ab7ba35ea3495dbc`.
Reempacotamento de JAR e sources reproduz os hashes originais em
qa/results/creative-tab-first-hashes.json; log creative-tab-reproducible.log.
Comparação com 6.0.2 exige preservar todos os recursos e classes não envolvidos;
resultado em creative-tab/production-diff.json. Hashes do pacote em SHA256SUMS.txt.

```powershell
.\gradlew.bat build --console=plain
.\gradlew.bat runProductionClientTest -PcreativeReview --console=plain
.\gradlew.bat runProductionClientTest -PcreativeReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Execute sequencialmente. Fixture usa os arquivos locais fornecidos e não é
redistribuído; preparação em qa/prepare_emf_extensions_fixture.py. Testes controlam
o cliente pela API, sem cliques no desktop. A seleção usa a API pública do Fabric
que troca também de página; selecionar só a aba pelo método privado vanilla é
cancelado pelo Fabric quando ela está em outra página. Os logs preliminares desse
teste falho foram preservados. Não foi uma falha no conteúdo registrado da aba.

Reconfiguração de spawners por ovo não foi testada nesta entrega.
Limites anteriores permanecem: shaders, todos os estilos em camadas, multiplayer,
modpack completo e profiling sem certificação. Não foi repetida toda a matriz
gráfica de tricórnio da 6.0.2; seus assets e adaptador de render não mudaram.
Não há Git/remoto neste diretório; entrega local, sem commit/push/release remota.
Instalação: substituir o JAR antigo do addon, sem duas versões simultâneas.
