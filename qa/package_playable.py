"""Close the explicitly requested playable-test milestone without certifying the whole spec."""
from pathlib import Path
import csv, hashlib, json, re, shutil, zipfile
import xml.etree.ElementTree as ET

R=Path(__file__).resolve().parents[1]; V='3.0.0-alpha.6'; O=R/'delivery'/V; O.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar'; sources=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/playable-3.0-build.log').read_text(encoding='utf-8'); client=(R/'qa/results/playable-3.0-client.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 35 required tests passed' in build
assert 'BUILD SUCCESSFUL' in client and 'after-server-restart count=48' in client and f'QA_ARTIFACT_SHA256 {sha(jar)} ' in client
first=json.loads((R/'qa/results/playable-first-hashes.json').read_text())
assert all(first[p.name]==sha(p) for p in (jar,sources))
tests=sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))
assert tests==31
archive=R/'qa/results/checkpoint-2.1.0-alpha.6'; archive.mkdir(exist_ok=True)
matrix=R/'qa/TEST_CASES.csv'
if not (archive/'TEST_CASES.csv').exists():shutil.copy2(matrix,archive/'TEST_CASES.csv')
rows=list(csv.DictReader((archive/'TEST_CASES.csv').open(encoding='utf-8')))
for row in rows:
    row['evidence']='qa/results/playable-3.0-build.log;qa/results/playable-3.0-client.log'
    row['notes']='Validação controlada por JUnit/GameTest e smoke de cliente; ver relatório para limites. '+row['notes']
    if row['tc_id'] in {'TC-BOOT-09','TC-SPAWN-06','TC-SPAWN-07','TC-CREW-06','TC-CREW-08','TC-CREW-09','TC-COMPAT-01'}:
        row.update(status='NOT RUN',notes='Cenário completo não repetido/certificado em 3.0. Smoke não inclui espera 120s, novo processo Java nem FO. Ordens, interinidade e persistência têm provas parciais de servidor; não certificar o caso completo.')
    if row['tc_id'] in {'TC-AI-02','TC-CREW-10','TC-SOC-07'}:
        row.update(status='PASS',notes='GameTest real: recuo ferido com alvo vivo; comando yield com saúde/inventário/autoridade; ou morte/testemunho/rumor presencial sem duplicação, conforme ID.')
with matrix.open('w',encoding='utf-8',newline='') as f:
    w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
counts={s:sum(r['status']==s for r in rows) for s in {'PASS','BLOCKED','NOT RUN'}}
report=f'''# Teste jogável {V} — 2026-11-03

Entrega parcial encerrada por solicitação do usuário para preservar a cota restante.
Não representa conclusão da especificação v2.0.

- Minecraft 26.2, Java 25, Loader 0.20.1, Loom 1.17.21, Gradle 9.5.1.
- MCA 8.1.11+26.2, Fish 26.2.2.0, Fabric API 0.155.2+26.2, YACL 3.9.7+26.2.
- **31 métodos JUnit PASS; 35 GameTests obrigatórios PASS** (34 addon, 1 fixture Fabric).
- Cliente de produção PASS: 48 adultos (24 por gênero), identidade servidor/cliente,
  diversidade, texturas, comandos/permissões, cargos, Nether, unload confirmado,
  reload de recursos e fechamento/reabertura do servidor integrado.
- Checkpoints de espera 30/120s e reinício de todo o processo não repetidos nesta
  rodada reduzida. O checkpoint 2.1 possui evidências anteriores separadas.
- Native SavedData/codec v3: importação Properties v2 preserva original/backup,
  gravação atômica, falha de escrita/retry, recuperação explícita e rejeição de schema futuro.
- Ordens com alvo/âncora, imediato interino, eventos persistentes, rumores por encontro,
  rendição e retirada de feridos possuem regressões novas. Detalhes em
  docs/PROGRESSO_3.0_ALPHA6.md e logs qa/results.
- Dois empacotamentos produziram JAR e sources idênticos. SHA256 do JAR: `{sha(jar)}`.

Matriz de 62 casos: {counts}. PASS pode ser prova de domínio/servidor;
não equivale a executar todos os cenários manualmente. NOT RUN não conta como PASS.

## Limitações

Bandeira/menu de fundação, tesouraria/recompensas/PARDON, integração completa dos
diálogos MCA, livro/HUD e diplomacia ainda não implementados. ATTACK aceita apenas
monstros nesta entrega. Não há profiling comparativo de dez minutos com 4/12/24/48.
Mundos históricos Alpha 1–5 não foram fornecidos; a migração foi validada por
fixtures e pelo mundo de testes anterior, não por esses saves históricos.
FO não foi repetido em 3.0; em 2.1 houve hang intermitente ao encerrar. Não há
certificação de compatibilidade completa. Conversões especiais/remoções diretas e
kill do processo durante transações não foram certificados.

Nenhum perfil/save pessoal foi alterado. EULA somente nos testes isolados autorizados.
Sem commit/push/release remota: diretório recebido não possui Git nem remoto.
'''
(R/'docs/RELATORIO_TESTE_JOGAVEL_3.0.md').write_text(report,encoding='utf-8')
readme='''# Pirates of MCA — teste jogável 3.0.0-alpha.6

Crie um perfil separado: Minecraft 26.2, Fabric Loader 0.20.1 e Java 25.
Copie os cinco JARs desta pasta mods para o perfil. Não instale o JAR de GameTests.
Use um mundo novo com cheats para o primeiro teste e conclua a tela inicial do MCA.

Em uma área segura, invoque dois piratas adultos:
```
/summon mca:male_villager ~2 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_bruiser","pmca_gender_male","pmca_adult_required"]}
/summon mca:female_villager ~4 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}
/pirates captain found
/pirates crew status
/pirates order nearby follow
```
Aguarde cerca de um segundo após os summons. Mire em um membro a até seis blocos:
`/pirates officer appoint nearest first_mate`. Experimente `hold`, `guard` e
`cease_fight`; ATTACK exige mirar em monstro hostil a até 24 blocos. NPCs podem
recusar ordens por lealdade/risco. Para sair: `/pirates captain resign`.

Teste visual/combate, afaste-se e retorne, salve e reabra. Esta é uma versão
experimental parcial: consulte o relatório; FO, migração histórica e desempenho
completo não estão certificados. Use cópia de saves importantes.
'''
(O/'LEIA-ME.md').write_text(readme,encoding='utf-8')
mods=O/'mods';mods.mkdir(exist_ok=True)
dependencies=list((R/'reference-jars').glob('*.jar'))
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
dependencies += [next((cache/'net.fabricmc.fabric-api/fabric-api/0.155.2+26.2').rglob('*.jar')),next((cache/'maven.modrinth/yacl/3.9.7+26.2-fabric').rglob('*.jar'))]
for p in [jar]+dependencies:shutil.copy2(p,mods/p.name)
for p in [jar,sources,matrix,R/'docs/RELATORIO_TESTE_JOGAVEL_3.0.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
def zipfiles(path,files,base):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(files):
            info=zipfile.ZipInfo(p.relative_to(base).as_posix(),(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16;z.writestr(info,p.read_bytes())
zipfiles(O/f'PiratesMCA-{V}-playtest.zip',[O/'LEIA-ME.md']+list(mods.glob('*.jar')),O)
files=[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
for name in ['source','src','qa','docs','gradle','reference-jars','baseline']:
    files += [p for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
zipfiles(O/f'piratesmca-{V}-workspace.zip',files,R)
checks=[p for p in O.rglob('*') if p.is_file() and p.name!='SHA256SUMS.txt']
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(O).as_posix()}\n' for p in sorted(checks)),encoding='utf-8')
print(json.dumps({'directory':str(O),'junit':tests,'gametests':35,'matrix':counts,'sha256':sha(jar)},indent=2))
