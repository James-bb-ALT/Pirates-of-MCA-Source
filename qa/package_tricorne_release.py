"""Package an experimental Lote 0 checkpoint, with visual gates left explicit."""
from pathlib import Path
import csv,hashlib,json,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='6.0.0-alpha.9';O=R/'delivery'/V
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';sources=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/tricorne-final-build.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 53 required tests passed' in build
for kind in ['off','fresh']:
    log=(R/f'qa/results/tricorne-client-{kind}.log').read_text(encoding='utf-8')
    assert all(s in log for s in ['BUILD SUCCESSFUL','QA_TRICORNE_VIEWS ','QA_MCA_HEAD_RENDER ','after-server-restart count=48',f'QA_ARTIFACT_SHA256 {sha(jar)} '])
    assert 'Missing textures in model piratesmca' not in log
    manifest=json.loads((R/f'qa/results/tricorne-client-{kind}-final-evidence/manifest.json').read_text());assert manifest['complete_process_exit']
tests=sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'));assert tests==35
first=json.loads((R/'qa/results/tricorne-first-hashes.json').read_text(encoding='utf-8-sig'));assert all(first[p.name]==sha(p) for p in [jar,sources])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/tricorne-reproducible.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/tricorne-assets.log').read_text(encoding='utf-8')
catalog=json.loads((R/'definitions/asset_catalog.json').read_text(encoding='utf-8'))
asset=next(a for a in catalog['assets'] if a['id']=='piratesmca:tricorne_classic');assert asset['status']=='exported' and asset['animation_safe'] is False
report=f'''# Tricórnio MVP — Lote 0 — {V}

Data: 2026-11-03. **Checkpoint experimental; certificação visual completa BLOCKED.**
O tricórnio funciona no cliente testado, mas a revisão no editor e a bateria de
poses ainda estão pendentes. Nenhum dos lotes 1–4 foi iniciado. Há 63 assets
planejados e um exportado; nenhum foi marcado ready/animation_safe.

## Entregue

- Fonte: models/blockbench/head/tricorne_classic.bbmodel — formato Blockbench 5.0.
- Referência: models/blockbench/reference_adult_mca.bbmodel — neutra, só editor.
- PNG original 64×64: textures/wearables/head/tricorne_classic.png.
- Export: exports/fabric_renderer/tricorne_classic.json — 15 cubos, faces com UV.
- Runtime: assets/piratesmca/models/item/tricorne_classic.json e textures/item.
- Preset: definitions/presets/tricorne_mvp.json; item/preset persistem no NBT nativo.
- Scripts: scripts/pmca/build_tricorne.py, validar_assets.py e tests/.
- Java: PirateWearables e três mixins client-only de estado/escala/camada da cabeça.

## Auditoria e escolha

Ver [DECISAO_RENDERER.md](renderer-audit/DECISAO_RENDERER.md) e os dumps javap reais.
MCA usa modelo humano PlayerModel, camadas independentes de pele/rosto/roupa/cabelo
e CustomHeadLayer herdada de HumanoidMobRenderer. A rota escolhida é equipamento
HEAD nativo com adaptador Fabric restrito à escala da cabeça do MCA. O export de
item só foi escolhido depois de identificar essa camada efetiva; não é uma
solução genérica de prótese. Nada substitui a skin base, o renderer ou o Fresh.

CEM/EMF permanece candidato **não demonstrado**: villager.jem vanilla não descreve
o corpo MCA. Com EMF/ETF/Fresh ativados, QA_MCA_HEAD_RENDER ainda registra ModelPart
na raiz/cabeça MCA nesta configuração; isso não prova impossibilidade universal,
nem suporte a outro pack CEM. Nenhum modelo Fresh foi copiado para o addon.

## Resultados

| ID | Verificação | Resultado/evidência |
|---|---|---|
| HAT-01 | Item HEAD, sem asset de armadura; atlas de item e UV corretos | PASS: GameTest, três testes Python, asserção do sprite no cliente |
| HAT-02 / TC-SPAWN-01/02/03 | Dois adultos de gêneros diferentes; pele/roupa/cabelo/nome preservados; criança comum excluída | PASS: servidor e snapshots de cliente |
| HAT-03 | Equipar/retirar por capitão em sobrevivência; negar estranho; preservar capacete existente | PASS: comandos reais no cliente e GameTest |
| HAT-04 / TC-SPAWN-05/06 | HEAD/preset após NBT, unload confirmado e reabrir servidor integrado | PASS: GameTest e duas rodadas de cliente |
| HAT-05 / TC-SAVE-03 | NPC equipado atravessa Nether e volta | PASS: rodada final Fresh seleciona explicitamente um portador do tricórnio |
| HAT-06 | Modelo real; referência editável coincide com seis peças capturadas em runtime | PASS: runtime-mca-neutral-off.json e comparação geométrica |
| HAT-07 | Frente, lado, costas sem EMF/ETF/Fresh | PASS limitado a idle: capturas off-final-evidence |
| HAT-08 | Mesmas vistas com EMF 3.3.11, ETF 7.2.5 e Fresh 1.11.2 | PASS limitado a idle e à combinação testada: fresh-final-evidence |
| HAT-09 | Abrir .bbmodel e revisar textura/edição no Blockbench | BLOCKED: editor web abriu, automação não conseguiu operar seu diálogo de arquivo |
| HAT-10 | Walk/run/attack/hurt/swim/sit/sleep e todos os cabelos/formatos | BLOCKED: bateria gráfica não executada |
| HAT-11 | Shaders, pack completo FO e todos os 344 JARs pessoais | BLOCKED: não executados nesta rodada |
| HAT-12 | Profiling com 4/12/24/48 portadores | BLOCKED: sem medição; smoke de 48 usa NoAI e dois chapéus |

Build real Java 25 / MC 26.2 / Loader 0.20.1 / Loom 1.17.21 / Gradle 9.5.1:
**35 JUnit, 53 GameTests obrigatórios (52 addon + 1 Fabric), três testes Python e
validador do kit PASS**. Clientes OFF/ON terminaram BUILD SUCCESSFUL. Não se infere
ausência de erros gerais: os logs têm falhas de autenticação do perfil de teste e
a suíte de servidor injeta corrupção deliberada para testar recuperação.

MCA 8.1.11+26.2, Fish 26.2.2.0, API 0.155.2+26.2 e YACL 3.9.7+26.2 preservados.
EMF/ETF e Fresh são cópias de teste isoladas dos arquivos presentes no perfil,
não foram redistribuídos no pacote. As capturas confirmam o pack Fresh carregado;
isso não certifica todo o modpack nem extensões Fresh de terceiros.

## Falhas encontradas e correções

1. **Textura ausente em jogo**: wearables/head ficava fora do atlas de itens.
   Causa confirmada pelo WARN Missing textures e capturas magenta. Runtime passou
   para textures/item; fonte PNG continua separada. Novo teste consulta o sprite
   real pelo AtlasIds.ITEMS e compara o identificador, além da regressão de caminho.
   Os logs/capturas pre-atlas registram FAIL gráfico, mesmo tendo BUILD SUCCESSFUL.
2. **UV em pixels não é UV vanilla**: export normaliza coordenadas para 0–16;
   testes amostram o PNG para impedir faces na metade transparente e validam dourado.
3. **Capturas iguais/falsas vistas**: comando de servidor sem jogador não movia a
   câmera; depois, inteiros X/Z eram centralizados em +0,5, invalidando a espera.
   Teste agora usa jogador real, coordenadas decimais e espera a posição no cliente.
   A câmera traseira foi afastada da fila de NPCs para evitar oclusão.
4. **API do teste de atlas**: getAtlasOrThrow recebe AtlasIds.ITEMS, não o caminho
   textures/atlas/items.png. Corrigido apenas no teste; log diagnóstico preservado.

## Instalar e experimentar

Substitua o JAR antigo do addon; não instale duas versões juntas. Seu perfil foi
inspecionado com Pirates 3.0, mas não foi modificado. Use cópia de mundo para o teste.

```
/give @s piratesmca:tricorne_classic
/pirates preset nearest tricorne_classic
/pirates preset nearest clear
/pirates debug identity
```

Preset exige pirata adulto próximo/visível, capitão da própria crew ou OP. Cabeça
ocupada não é sobrescrita. O preset cosmético não concede defesa nem muda cargo.
Para spawn de teste, OP pode acrescentar pmca_preset_tricorne_classic às tags de
um pirata adulto marcado. Não há re-equipamento por tick depois de retirar o item.
Equipar manualmente em todas as interfaces MCA e uso no jogador não têm prova
gráfica específica nesta rodada; a rota validada é o preset no NPC.

Abra a fonte no Blockbench e edite cubos/UV; o PNG externo é a fonte da textura.
Não use o gerador para substituir modelos já editados: ele cria a fonte só se
estiver ausente e exporta a existente. Rotações não suportadas são recusadas.

```
python scripts/pmca/build_tricorne.py
python scripts/pmca/validar_assets.py --root .
.\\gradlew.bat clean build --console=plain
.\\gradlew.bat runProductionClientTest -PplayableSmoke --console=plain
.\\gradlew.bat runProductionClientTest -PplayableSmoke -PcompatMods=qa/fixtures/tricorne-emf/mods --console=plain
```

Executar sequencialmente. O fixture EMF/Fresh não é parte pública do source ZIP;
para reproduzir, fornecer os mesmos arquivos de sua instalação e copiar Fresh
para overrides/resourcepacks, com options.txt isolado que habilite esse pack.
As instruções não exigem substituir arquivos originais MCA/Fresh.

## Evidências e gates pendentes

Logs qa/results/tricorne-final-build.log, tricorne-client-off.log e
tricorne-client-fresh.log; JUnit XML em tricorne-junit. Cada pasta final-evidence
tem coorte de 48 UUIDs e manifesto SHA256. A geometria real está em renderer-audit.
As capturas preliminares de textura ausente permanecem separadas das finais.

Não foi possível certificar abertura do bbmodel no editor. Blockbench Desktop não
foi encontrado; o web carregou por navegador nativo, mas o seletor modal recusou
entrada automatizada (cache de elemento/janela alvo divergente). Não é evidência
de que o arquivo seja inválido, nem de que abra corretamente. Nenhum arquivo foi
enviado com sucesso pelo seletor. A aba de revisão ficou disponível ao usuário.

A [skill usada](../.agents/skills/pirates-mca-blockbench/SKILL.md) determina
“Produzir um asset por vez”; seu quality-gates.md exige “Source .bbmodel editável
salvo e abre com texturas.” Interpretação aplicada: não iniciar outro asset/lote
enquanto esta revisão P0 e a aceitação visual estiverem pendentes. Não foi pedido
consentimento adicional nem instalado Blockbench automaticamente.

JAR testado/entregue: `{sha(jar)}`. JAR e sources reempacotados com bytes idênticos;
hashes em tricorne-first-hashes.json e log tricorne-reproducible.log. Sem stubs.
Sem release remota/commit/push: não há Git nem remoto neste diretório.
Funcionalidades sociais/costeiras anteriores e dados existentes foram preservados.
Esta entrega é um checkpoint, não uma declaração de conclusão da especificação.
'''
(R/'reports/tricorne_mvp.md').write_text(report,encoding='utf-8')
O.mkdir(parents=True,exist_ok=True)
readme=f'''# Pirates of MCA {V} — checkpoint Lote 0

Tricórnio funcional em teste real; revisão no Blockbench e poses completas BLOCKED.
Leia tricorne_mvp.md antes de tratar como release visual pronta.

Minecraft 26.2, Java 25, Fabric Loader 0.20.1, MCA 8.1.11+26.2,
Fish 26.2.2.0, API e YACL das entregas anteriores. Substitua somente o addon antigo.
Não instale o JAR de GameTests; EMF/Fresh opcionais não estão redistribuídos aqui.

/give @s piratesmca:tricorne_classic
/pirates preset nearest tricorne_classic
/pirates preset nearest clear

Preset: pirata adulto a até seis blocos, cabeça vazia e comando do capitão/OP.
Fonte editável e PNG no ZIP de assets; scripts e testes no source ZIP.
Próximo gate: abrir models/blockbench/head/tricorne_classic.bbmodel no Blockbench
e revisar cabelo/poses. Os lotes 1–4 permanecem planejados.
'''
(O/'LEIA-ME.md').write_text(readme,encoding='utf-8')
for p in [jar,sources,R/'reports/tricorne_mvp.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
def zipfiles(path,files,base):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(set(files)):
            info=zipfile.ZipInfo(p.relative_to(base).as_posix(),(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(info,p.read_bytes())
assets=[]
for name in ['models','textures','definitions','exports','scripts','reports','art']:
    assets += [p for p in (R/name).rglob('*') if p.is_file() and '__pycache__' not in p.parts and p.suffix!='.pyc']
zipfiles(O/f'piratesmca-{V}-assets.zip',assets,R)
files=[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]+assets
for name in ['.agents','source','src','qa','docs','gradle','reference-jars','baseline']:
    files += [p for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
zipfiles(O/f'piratesmca-{V}-workspace.zip',files,R)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(O).as_posix()}\n' for p in sorted(O.rglob('*')) if p.is_file() and p.name!='SHA256SUMS.txt'),encoding='utf-8')
print(json.dumps({'directory':str(O),'junit':tests,'gametests':53,'python':3,'full_visual_gate':'BLOCKED','sha256':sha(jar)},indent=2))
