"""Package the scoped dialogue release and verify preservation against alpha.17."""
from pathlib import Path
import hashlib,json,shutil,subprocess,zipfile,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1]
V='12.0.0-alpha.18'
out=R/'delivery'/V;out.mkdir(parents=True,exist_ok=True)
old=R/'build/libs/piratesmca-11.1.1-alpha.17.jar'
new=R/f'build/libs/piratesmca-{V}.jar'
with zipfile.ZipFile(old) as a,zipfile.ZipFile(new) as b:
    protected=[n for n in a.namelist() if n.endswith('.png') or '/models/' in n or '/equipment/' in n]
    changed=[n for n in protected if n not in b.namelist() or a.read(n)!=b.read(n)]
    assert not changed,changed[:10]
    for lang in ('en_us','pt_br'):
        n=f'assets/piratesmca/lang/{lang}.json';before=json.loads(a.read(n));after=json.loads(b.read(n))
        assert all(after.get(k)==v for k,v in before.items())
    assert not any('DialogueClientReview' in n for n in b.namelist())
units=[ET.parse(p).getroot() for p in (R/'build/test-results/test').glob('TEST-*.xml')]
assert sum(int(x.attrib['tests']) for x in units)==44
assert all(int(x.attrib['failures'])+int(x.attrib['errors'])==0 for x in units)
log=(R/'qa/results/dialogues-final.log').read_text(encoding='utf-8',errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'DLG_CLIENT_PASS' in log and 'DLG_NATIVE_TRADE_PASS' in log
report=R/'reports/dialogues.md'
report.write_text(f'''# Diálogos — {V} — 2026-11-05

## Uso e escopo

Instale somente o JAR principal em mods, substituindo o Pirates of MCA anterior. Requer Minecraft 26.2, Java 25, Fabric Loader 0.20.1, Fabric API, MCA 8.1.11+26.2 e Fish of Thieves 26.2.2.0.
Shift + clique direito com mão principal vazia em um pirata ou /pirates talk abre assuntos. Avisos automáticos oferecem Responder sem abrir uma janela à força. O idioma acompanha en_us/pt_br do cliente.

Importadas 216 falas, nove cenas e 27 escolhas: 279 traduções originais por idioma. Somente adultos vivos marcados como piratas e com profissão Pirata participam. Os nós MCA fornecidos ficam arquivados, fora do roteador global. Preserva interação, comércio e presentes MCA.

## Execução

| Verificação | Resultado | Evidência |
|---|---|---|
| Build Java 25 / Loom com dependências reais | PASS | dialogues-final.log, BUILD SUCCESSFUL |
| JUnit: 44 testes, zero falhas | PASS | XML de build/test-results/test |
| Validadores de assets e testes Python | PASS | tarefas validatePirateAssets, validateTricorneExport e validateAtelierAssets |
| Nove cenas em cliente Minecraft real | PASS | DLG_AUTO_CONSPIRACY_PASS e oito DLG_SCENE_PASS |
| EN/PT, isolamento de NPC comum | PASS | DLG_LOCALE_PASS e DLG_ISOLATION_PASS |
| Presente e loja nativos MCA | PASS | DLG_NATIVE_GIFT_PASS / DLG_NATIVE_TRADE_PASS |
| Resposta repetida, token falso e distância | PASS | DLG_AUTO_CONSPIRACY_PASS / DLG_PERSISTENCE_RANGE_PASS |
| Histórico/cooldown via save/load nativo da entidade | PASS | DLG_PERSISTENCE_RANGE_PASS e testes unitários |
| Preservação de {len(protected)} assets anteriores PNG/modelos/equipment | PASS | comparação binária com alpha.17; traduções antigas preservadas |
| Reiniciar jogo, descarregar chunks e mudar dimensão com conversa ativa | BLOCKED | não executado nesta rodada |
| Modpack FO completo e multiplayer com clientes de idiomas distintos | BLOCKED | não executado nesta rodada |
| Profiling de 4/12/24/48 NPCs | BLOCKED | não executado nesta rodada |

## Gatilhos

| Cena | Condição real |
|---|---|
| mutiny_whisper | fase de conspiração/ultimato/rebelião, conspirador vivo presente e observador leal |
| captain_test | tripulação sem capitão |
| missing_crate | jogador quebra recipiente perto da bandeira da tripulação |
| damaged_hull | dano real em barco ocupado por pirata ou capitão da tripulação |
| share_the_find | tesouro na mão ou presente aceito pelo MCA |
| voyage_invitation | ordem FOLLOW e embarcação |
| lost_sailor | colega vivo carregado, separado mais de 24 blocos e dentro da água |
| deck_trespass | jogador estrangeiro próximo da bandeira |
| rival_flag | outra bandeira de tripulação vinculada e fisicamente próxima |

Doze contextos: cumprimento, conversa, mar, tesouro, comércio, capitão, tripulação, lealdade, rumor, perigo, presente e despedida. Ordens, sucessão e conflitos sociais usam eventos reais da tripulação. Falas específicas de tempestade, casco, rivalidade e conspiração exigem seus sinais. Avisos urgentes de conspiração podem usar o texto fornecido independentemente do temperamento; as demais cenas respeitam seu tom.

## Persistência e limites

Memória limitada às últimas quatro falas e quatro eventos, com cooldown persistido nas tags nativas. Sessões são temporárias, vinculadas a jogador/NPC/tripulação e revalidam dimensão, alcance, validade e contexto. Respostas não concedem capitania nem iniciam combate; efeitos sociais têm limite de uma aplicação por 60 segundos. Um NPC descarregado não é considerado perdido ou morto.

Uma escolha por cena foi executada em jogo; as 27 alternativas foram verificadas estruturalmente, não todas clicadas. Os testes preparam fixtures e reiniciam o cooldown de fala entre cenas, sem alterar o cooldown de produção. Textos narrativos do pack não implementam missões, recompensas, resgate automático ou reparo físico de barcos. Sem mudança no schema da tripulação.

Houve tentativas anteriores com falha no teste de codificação PT e na fixture de marinheiro fora de chunk carregado; corrigidas e reexecutadas. Uma execução intermediária foi cancelada para evitar testar compilação anterior ao hook de dano. A primeira captura visual ocorreu durante o overlay de reload; as capturas foram refeitas aguardando o overlay desaparecer. Logs finais e de recaptura acompanham as evidências.

## Reprodução

gradlew.bat jar sourcesJar test validatePirateAssets validateTricorneExport validateAtelierAssets runProductionClientTest -PdialogueReview --daemon

O teste usa run/dialogue-test isolado. Não usa stubs. O importador scripts/pmca/import_dialogues.py recebe --zip; não executa bytecode do ZIP nem ativa cenas globalmente no MCA.
''',encoding='utf-8')
for name in (f'piratesmca-{V}.jar',f'piratesmca-{V}-sources.jar'):
    shutil.copy2(R/'build/libs'/name,out/name)
modified=subprocess.check_output(['git','diff','--name-only'],cwd=R,text=True).splitlines()
extras=['qa/DialogueLogicTest.java','qa/package_dialogues.py','scripts/pmca/import_dialogues.py','reports/dialogues.md','definitions/dialogue_ui_en_us.json','definitions/dialogue_ui_pt_br.json','src/gametest/java/dev/arandu/piratesmca/DialogueClientReview.java']
extras += [str(p.relative_to(R)).replace('\\','/') for base in ['imports/pirate_dialogues_v1','source/src/main/resources/data/piratesmca/pirate_dialogues'] for p in (R/base).rglob('*') if p.is_file() and p.suffix!='.pyc']
extras += [str(p.relative_to(R)).replace('\\','/') for p in (R/'source/src/main/java/dev/arandu/piratesmca').glob('PirateDialogue*.java')]
extras += ['source/src/main/java/dev/arandu/piratesmca/mixin/PirateBoatDialogueMixin.java','source/src/main/java/dev/arandu/piratesmca/mixin/PirateConversationMixin.java']
paths=sorted(set(modified+extras));(out/'TASK_FILES.json').write_text(json.dumps(paths,indent=2),encoding='utf-8')
with zipfile.ZipFile(out/f'piratesmca-{V}-source-delta.zip','w',zipfile.ZIP_DEFLATED,9) as z:
    for name in paths:z.write(R/name,name)
with zipfile.ZipFile(out/f'piratesmca-{V}-evidence.zip','w',zipfile.ZIP_DEFLATED,9) as z:
    for name in ['qa/results/dialogues-final.log','qa/results/dialogues-capture.log','reports/dialogues.md']:
        if (R/name).exists():z.write(R/name,name)
    for base in ['run/dialogue-test/screenshots','build/test-results/test']:
        for p in (R/base).rglob('*'):
            if p.is_file():z.write(p,str(p.relative_to(R)))
shutil.copy2(report,out/'dialogues.md')
(out/'SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(out.iterdir()) if p.is_file() and p.name!='SHA256SUMS'),encoding='utf-8')
print('PACKAGED',out,'preserved_assets=',len(protected))
