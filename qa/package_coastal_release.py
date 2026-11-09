"""Package coastal generation with scoped, verifiable evidence."""
from pathlib import Path
import csv,hashlib,json,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='5.0.0-alpha.8';O=R/'delivery'/V
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';sources=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/coastal-final-build.log').read_text(encoding='utf-8')
client=(R/'qa/results/coastal-client.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 49 required tests passed' in build
assert all(s in client for s in ['BUILD SUCCESSFUL','after-server-restart count=48','QA_FLAG_DIALOG_FOUNDED ',f'QA_ARTIFACT_SHA256 {sha(jar)} '])
tests=sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'));assert tests==35
first=json.loads((R/'qa/results/coastal-first-hashes.json').read_text(encoding='utf-8-sig'));assert all(first[p.name]==sha(p) for p in [jar,sources])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/coastal-reproducible.log').read_text(encoding='utf-8')
assert json.loads((R/'qa/results/coastal-client-evidence/manifest.json').read_text())['complete_process_exit']
report=f'''# Estruturas costeiras — {V} — 2026-11-03

Extensão funcional autorizada pelo usuário: villagers gerados por qualquer
estrutura em praia/oceano devem virar piratas. Inclui a inspeção do modpack local.
Esta extensão prevalece sobre a restrição anterior de adaptador apenas Fish;
docs/ESPECIFICACAO_ORIGINAL_v2.0.md permanece como registro histórico imutável.

## Regra implementada

Durante StructureStart.placeInChunk, villagers vanilla ou MCA adicionados por
ServerLevel/WorldGenRegion em `piratesmca:pirate_waters` recebem as tags de pirata.
O contexto termina em finally, inclusive em geração aninhada ou com exceção.
Não há busca por estruturas em cada tick nem conversão só por estar perto do mar.
Modos normais de reprodução/comando não passam por esse contexto de geração.
O bioma é avaliado na posição de spawn do NPC; estruturas que cruzam biomas podem
ter conversão parcial. Há uma crew por dimensão, ID de estrutura e chunk de origem.
NPCs de seaposts Fish preservam o adaptador e as origens anteriores.

A tag aceita oceanos/praias Minecraft e c, variedades de oceano profundo/raso,
costa rochosa, KS deeper_cold_ocean, Rawlands mist_coast e ilha tropical Fish.
Entradas opcionais não exigem instalar esses mods. A ilha tropical é uma extensão
temática explícita. Um datapack pode ajustar a tag sem alterar código.

## Execução e resultados

| Caso | Resultado | Escopo/evidência |
|---|---|---|
| TC-BOOT-01: clean build real | PASS | coastal-final-build.log; 35 JUnit; 49 GameTests obrigatórios, sendo 48 addon e 1 Fabric |
| COAST-01: oceano | PASS | 4 NPCs por teste, 2 vanilla convertidos MCA e 2 MCA diretos, duas crews distintas |
| COAST-02: praia | PASS | mesmo pipeline StructureStart e addFreshEntity |
| COAST-03: bioma de mod via tag c | PASS | fixture piratesmca-tests:coast |
| COAST-04: planícies | PASS | 4 NPCs preservados, zero piratas |
| COAST-05: ilha tropical Fish | PASS | bioma real fishofthieves:tropical_island |
| COAST-06: isolamento e limpeza do contexto | PASS | crianças comuns fora da estrutura preservadas; exceção de geração não contamina spawn posterior |
| TC-SPAWN-10 parcial: template Fish real | PASS parcial | 2 adultos com nome, pele, roupa e capitão; teste de servidor |
| Spawn comum vanilla/MCA | PASS | 36 NPCs comuns, 12 crianças preservadas, fora do contexto da estrutura |
| Cliente com JAR de produção | PASS | 48 piratas via comandos, identidade/texturas, bandeira, reload, Nether, unload e reabertura integrada |
| Colocação costeira por todos os mods do perfil | NOT RUN | Os 344 JARs não foram executados juntos nesta rodada |
| Mundo oceânico percorrido, geração natural por seed | NOT RUN | Testes usam peças controladas e template Fish real |
| Reinício de todo o processo cliente nesta versão | NOT RUN | Não inferido da reabertura integrada |
| FO completo, multiplayer, profiling 4/12/24/48 por dez minutos | NOT RUN | Não certificado |
| Saves históricos Alpha 1–5 | BLOCKED | Não fornecidos |

As peças genéricas de teste são fixtures dentro de StructureStart registrado como
shipwreck; não são a prova de que todos os templates de todos os mods foram
executados. O caminho WorldGenRegion é instrumentado e carrega sem erro de mixin,
mas as novas asserções funcionais exercitam ServerLevel. Templates vanilla/mods
que adicionam villagers por essas APIs durante a geração entram no mecanismo.
Spawns agendados para depois da geração, sistemas próprios que bypassam essas APIs,
villagers não compatíveis com MCA e estruturas não registradas exigem avaliação.

## Biomas do perfil

Perfil Fabulously Optimized: 344 JARs ativos, 294 IDs com definições JSON, 18
definições cobertas pela regra. É inventário estático de recursos, não dump do
registro vivo; referências opcionais sem definição encontrada não contam como
biomas confirmados. Ver BIOMAS_DO_MODPACK.md e qa/results/pack-biomes/biomes.csv.
`joshie:village_ocean` do Tidal Towns é candidata identificada, não validada em jogo.
O perfil ainda contém Pirates 3.0.0-alpha.6; nenhum perfil pessoal foi alterado.

## Artefatos e reprodução

Java 25 / Minecraft 26.2 / Loader 0.20.1 / Loom 1.17.21 / Gradle 9.5.1;
MCA 8.1.11+26.2, Fish 26.2.2.0, API 0.155.2+26.2, YACL 3.9.7+26.2.
Sem stubs; dependências não foram alteradas. SavedData continua no schema v4.

```
.\\gradlew.bat clean build --console=plain
.\\gradlew.bat runProductionClientTest -PplayableSmoke --console=plain
python qa/audit_release.py
```

Executar sequencialmente. Logs: qa/results/coastal-final-build.log e coastal-client.log.
JUnit XML: qa/results/coastal-junit. Capturas: qa/results/coastal-client-evidence.
JAR entregue/testado: `{sha(jar)}`. Reempacotamento produziu JAR/sources idênticos;
coastal-first-hashes.json e coastal-reproducible.log registram os dois passes.
Isso não comprova build offline em outra máquina.

## Limitações restantes

**A regra vale para novas gerações. Não reclassifica automaticamente NPCs de
estruturas que já estavam salvas.** Isso evita transformar moradores transportados
ou nascidos depois em estruturas antigas. Seaposts já marcados continuam funcionando.
Não há migração automática desse novo critério para barcos antigos nesta entrega.
Faltam tesouraria/PARDON, diplomacia, Livro/HUD e integração completa de diálogos MCA.
ATTACK continua restrito a monstros. Smoke visual de 48 usa NoAI; não mede IA.
Advertências/erros deliberados de recuperação e autenticação do perfil de teste
permanecem nos logs; não se declara execução sem erros apenas por BUILD SUCCESSFUL.

Entrega parcial; preservadas as funcionalidades e os checkpoints anteriores.
Nenhum mundo/perfil pessoal foi alterado. EULA só nos testes isolados autorizados.
Sem commit/push/tag/release remota: diretório recebido não possui Git nem remoto.
'''
(R/'docs/RELATORIO_COSTEIRO_5.0.md').write_text(report,encoding='utf-8')
rows=[('COAST-01','TC-SPAWN-10','Oceano; duas estruturas, quatro villagers','PASS'),('COAST-02','TC-SPAWN-10','Praia; duas estruturas, quatro villagers','PASS'),('COAST-03','TC-SPAWN-10','Bioma de mod via tag comum','PASS'),('COAST-04','TC-AI-03','Planície; zero conversões, escopo spawn','PASS'),('COAST-05','TC-SPAWN-10','Ilha tropical Fish','PASS'),('COAST-06','TC-SPAWN-10;TC-AI-03','Contexto encerra em exceção; criança comum preservada','PASS'),('COAST-07','TC-SPAWN-10','Geração natural por seed e todos os mods','NOT RUN')]
with (R/'qa/COASTAL_TEST_CASES.csv').open('w',encoding='utf-8',newline='') as f:
    w=csv.writer(f);w.writerow(['id','contract_ids','scenario','status']);w.writerows(rows)
O.mkdir(parents=True,exist_ok=True);mods=O/'mods';mods.mkdir(exist_ok=True)
readme=f'''# Pirates of MCA {V}

Java 25, Minecraft 26.2, Fabric Loader 0.20.1. Perfil separado para o primeiro teste.
O perfil pessoal auditado ainda usa Pirates 3.0.0-alpha.6: remova o JAR antigo ao
instalar este. Não mantenha duas versões de piratesmca na pasta mods.
O ZIP de teste contém cinco JARs; não duplique dependências já instaladas no perfil.

Explore chunks novos: villagers gerados por estruturas em praias/oceanos
classificados na tag pirate_waters viram piratas MCA. Não converte moradores
comuns só por estarem perto do mar, nem NPCs já salvos em estruturas antigas.

Para localizar seapost: /locate structure fishofthieves:seapost
Para a vila Tidal Towns, se o mod estiver instalado: /locate structure joshie:village_ocean
Tidal Towns foi identificado por inspeção, sem validação do pack completo nesta rodada.

Bandeira: /give @s piratesmca:pirate_flag; coloque perto de dois adultos livres
e amigáveis e clique com botão direito. Comandos anteriores preservados.
Veja RELATORIO_COSTEIRO_5.0.md e BIOMAS_DO_MODPACK.md para resultados e limitações.
'''
(O/'LEIA-ME.md').write_text(readme,encoding='utf-8')
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
deps=list((R/'reference-jars').glob('*.jar'))+[next((cache/'net.fabricmc.fabric-api/fabric-api/0.155.2+26.2').rglob('*.jar')),next((cache/'maven.modrinth/yacl/3.9.7+26.2-fabric').rglob('*.jar'))];assert len(deps)==4
for p in [jar]+deps:shutil.copy2(p,mods/p.name)
for p in [jar,sources,R/'qa/TEST_CASES.csv',R/'qa/COASTAL_TEST_CASES.csv',R/'qa/results/pack-biomes/biomes.csv',R/'docs/BIOMAS_DO_MODPACK.md',R/'docs/RELATORIO_COSTEIRO_5.0.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
def zipfiles(path,files,base):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(files):
            info=zipfile.ZipInfo(p.relative_to(base).as_posix(),(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(info,p.read_bytes())
zipfiles(O/f'PiratesMCA-{V}-playtest.zip',[O/'LEIA-ME.md']+list(mods.glob('*.jar')),O)
files=[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
for name in ['source','src','qa','docs','gradle','reference-jars','baseline']:
    files += [p for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
zipfiles(O/f'piratesmca-{V}-workspace.zip',files,R)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(O).as_posix()}\n' for p in sorted(O.rglob('*')) if p.is_file() and p.name!='SHA256SUMS.txt'),encoding='utf-8')
print(json.dumps({'directory':str(O),'junit':tests,'gametests':49,'sha256':sha(jar)},indent=2))
