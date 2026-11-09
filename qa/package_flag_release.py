"""Package the flag milestone only after real server and production-client gates."""
from pathlib import Path
import csv, hashlib, json, shutil, zipfile
import xml.etree.ElementTree as ET

R=Path(__file__).resolve().parents[1]
V=json.loads((R/'source/src/main/resources/fabric.mod.json').read_text())['version']
assert V=='4.0.0-alpha.7'
O=R/'delivery'/V
O.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar'
sources=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/flag-final-build.log').read_text(encoding='utf-8')
client=(R/'qa/results/flag-client-validation.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 41 required tests passed' in build
assert all(s in client for s in ['BUILD SUCCESSFUL','QA_FLAG_DIALOG_FOUNDED ','after-server-restart count=48',f'QA_ARTIFACT_SHA256 {sha(jar)} '])
first=json.loads((R/'qa/results/flag-first-hashes.json').read_text(encoding='utf-8-sig'))
assert all(first[p.name]==sha(p) for p in (jar,sources)), 'Rebuilt artifacts differ'
assert 'BUILD SUCCESSFUL' in (R/'qa/results/flag-reproducible.log').read_text(encoding='utf-8')
tests=sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))
assert tests==35
assert json.loads((R/'qa/results/flag-client-evidence/manifest.json').read_text())['complete_process_exit']
report=f'''# Bandeira e fundação — {V} — 2026-11-03

Entrega parcial da especificação v2.0. Os contratos originais não foram alterados.
Nova etapa de release conforme a classificação do usuário.

## Execução

| Validação | Resultado | Evidência |
|---|---|---|
| TC-BOOT-01: clean build real Java 25/Loom/MC 26.2 | PASS | qa/results/flag-final-build.log |
| 35 métodos JUnit | PASS | qa/results/flag-junit; mesmo log |
| 41 GameTests obrigatórios (40 addon, 1 Fabric) | PASS | mesmo log |
| TC-BOOT-02: cliente de produção, mundo integrado | PASS | qa/results/flag-client-validation.log |
| Fundação pelo botão real da bandeira; dois voluntários | PASS | QA_FLAG_DIALOG_FOUNDED; capturas FLAG |
| Ordem HOLD pelo menu | PASS | asserção de evento; aceitação continua contextual |
| 48 adultos, identidades/texturas e reload de recursos | PASS | mesmo log; qa/results/flag-client-evidence |
| Troca de dimensão, unload confirmado, fechar/reabrir servidor integrado | PASS | mesmo log, checkpoints de identidade |
| Bandeira e vínculo após reabrir o mundo | PASS | asserções do cliente |
| Reinício de todo o processo Java nesta versão | NOT RUN | Não inferido da reabertura integrada |
| TC-CREW-08 completo e multiplayer/desconexão | NOT RUN | Sete botões não certificam todos os cenários de ordens |
| FO 14.1.1 / compatibilidade completa nesta versão | NOT RUN | Resultado histórico não transferido |
| Profiling 4/12/24/48 por dez minutos | NOT RUN | Smoke de 48 usa NoAI; não mede custo da IA |
| Mundos históricos Alpha 1–5 | BLOCKED | Saves históricos não fornecidos |

Stack: Java 25, Minecraft 26.2, Loader 0.20.1, Loom 1.17.21, Gradle 9.5.1,
MCA 8.1.11+26.2, Fish 26.2.2.0, Fabric API 0.155.2+26.2 e YACL 3.9.7+26.2.
Compilação e validação usam os JARs reais; nenhum stub valida esta entrega.
JAR testado no cliente e entregue: `{sha(jar)}`.
Novo empacotamento de JAR e sources produziu bytes idênticos; hashes e log em
qa/results/flag-first-hashes.json e flag-reproducible.log. Não é prova de build
offline em outra máquina.

## Implementação e regressões

FLAG-01: bloco, item, receita real, drop e diálogo nativo. Antes faltava a rota
de fundação por bandeira; agora a interação abre o menu e cria a tripulação.
FLAG-02: consentimento, dois adultos livres, autoridade e propriedade exclusiva.
FLAG-03: ações recusadas se distante, com parede ou bandeira removida; comandos
recebidos pela tela são revalidados no servidor.
FLAG-04: regressão de servidor comprova a âncora de RALLY; domínio comprova estado
preservado e duplicatas rejeitadas. GUARD usa a mesma âncora na implementação,
mas a movimentação GUARD nesta âncora não tem teste dedicado. Testes não equivalem
a certificação de navegação prolongada em todas as condições.
FLAG-05: SavedData v3 migra para v4, mantendo o estado e uma cópia
`crews.dat.pre-v4.bak`. O vínculo sobrevive à reabertura do mundo de teste.

Classes: FlagLedgerTest, FlagGameTests, NativePersistenceGameTests e
PirateClientGameTests. A planilha FLAG_TEST_CASES.csv mapeia a extensão aos IDs
TC-BOOT-01/02 e TC-CREW-01/06/08. A matriz original de 62 casos conserva evidências
e limitações históricas; não é uma certificação nova de todos os casos.

Falhas durante o desenvolvimento ficaram nos logs preliminares: cena GameTest com
parede/posição inválida e helper Fabric que reconhece Button, mas não todos os
AbstractButton usados pelo diálogo. A cena e o helper do teste foram corrigidos;
as validações de alcance/visibilidade do produto foram preservadas.

## Limitações

Tesouraria/recompensas/PARDON, diplomacia, Livro de Bordo/HUD e integração completa
dos diálogos MCA permanecem pendentes. ATTACK continua via comando com alvo monstro.
O comando legado captain found permanece sem bandeira para preservar a função e
o contrato de teste anteriores; o consentimento novo é aplicado à rota da bandeira.
Quebrar a bandeira não dissolve a crew; recolocar no mesmo ponto recupera o vínculo.
Cada crew possui uma âncora ativa; vincular outra libera o ponto anterior.
Modelo e textos EN foram inspecionados em tela; PT tem recursos, sem rodada visual PT.
O formato v4 não deve ser aberto por versões antigas: elas rejeitam schema futuro.
Migração sintética não comprova compatibilidade de todos os saves históricos.
FO teve hang intermitente no encerramento de uma versão anterior; sem novo resultado.
O smoke registrou atraso de 13,3 s durante a sequência de viagens/chunks; sem
profiling não é possível atribuir a causa nem certificar desempenho. O perfil de
teste também registrou falhas de autenticação Realms/perfil (HTTP 401) e opção
gráfica de anisotropia inválida. A rodada concluiu, mas o log não está sem erros.
Drop e obtenção pela aba criativa estão implementados, sem teste de interação dedicado.

Nenhum perfil/save pessoal foi alterado. EULA somente nos testes isolados autorizados.
Sem commit/push/tag/release remota: diretório não possui Git nem remoto.
'''
(R/'docs/RELATORIO_BANDEIRA_4.0.md').write_text(report,encoding='utf-8')
rows=[
('FLAG-01','TC-BOOT-02;TC-CREW-01','Bloco/receita/diálogo e fundação pelo menu','PASS','FlagGameTests;PirateClientGameTests'),
('FLAG-02','TC-CREW-01','Consentimento e autoridade; sem roubo ou dupla vinculação','PASS','FlagLedgerTest;FlagGameTests'),
('FLAG-03','TC-CREW-01','Recusar interação distante, obstruída ou removida','PASS','FlagGameTests'),
('FLAG-04','TC-CREW-08','Âncora de RALLY e persistência; escopo servidor/domínio','PASS','FlagLedgerTest;FlagGameTests'),
('FLAG-05','TC-CREW-06','Migração v3/v4, backup e reabertura integrada','PASS','NativePersistenceGameTests;PirateClientGameTests')]
with (R/'qa/FLAG_TEST_CASES.csv').open('w',encoding='utf-8',newline='') as f:
    w=csv.writer(f);w.writerow(['id','contract_ids','scenario','status','evidence']);w.writerows(rows)
readme=f'''# Pirates of MCA {V} — teste jogável parcial

Perfil separado: Minecraft 26.2, Java 25 e Fabric Loader 0.20.1.
Copie os cinco JARs em mods. Use mundo novo com cheats; conclua a tela inicial MCA.

```
/summon mca:male_villager ~2 ~ ~ {{Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_male","pmca_adult_required"]}}
/summon mca:female_villager ~4 ~ ~ {{Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}}
/give @s piratesmca:pirate_flag
```

Aguarde um segundo, coloque a bandeira perto dos dois e clique com o botão direito.
Confirme a fundação; abra novamente para ordens. Se não houver dois voluntários,
aproxime os NPCs e confira amizade, idade e se já pertencem a outra tripulação.
Receita: BBW / BBS / ..S (B=lã preta, W=osso, S=graveto, .=vazio).

Comandos anteriores: /pirates captain found; /pirates crew status;
/pirates order nearby follow; /pirates officer appoint nearest first_mate.
Ordens podem ser recusadas conforme o estado social. Use cópia dos saves.
Consulte RELATORIO_BANDEIRA_4.0.md: FO, profiling e especificação completa pendentes.
'''
(O/'LEIA-ME.md').write_text(readme,encoding='utf-8')
mods=O/'mods';mods.mkdir(exist_ok=True)
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
deps=list((R/'reference-jars').glob('*.jar'))+[next((cache/'net.fabricmc.fabric-api/fabric-api/0.155.2+26.2').rglob('*.jar')),next((cache/'maven.modrinth/yacl/3.9.7+26.2-fabric').rglob('*.jar'))]
assert len(deps)==4
for p in [jar]+deps:shutil.copy2(p,mods/p.name)
for p in [jar,sources,R/'qa/TEST_CASES.csv',R/'qa/FLAG_TEST_CASES.csv',R/'docs/RELATORIO_BANDEIRA_4.0.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
def zipfiles(path,files,base):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(files):
            info=zipfile.ZipInfo(p.relative_to(base).as_posix(),(2026,11,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(info,p.read_bytes())
zipfiles(O/f'PiratesMCA-{V}-playtest.zip',[O/'LEIA-ME.md']+list(mods.glob('*.jar')),O)
files=[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
for name in ['source','src','qa','docs','gradle','reference-jars','baseline']:
    files += [p for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
zipfiles(O/f'piratesmca-{V}-workspace.zip',files,R)
checks=[p for p in O.rglob('*') if p.is_file() and p.name!='SHA256SUMS.txt']
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(O).as_posix()}\n' for p in sorted(checks)),encoding='utf-8')
print(json.dumps({'directory':str(O),'junit':tests,'gametests':41,'sha256':sha(jar)},indent=2))
