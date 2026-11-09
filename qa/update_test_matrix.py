"""Maintain explicit contract coverage; no PASS inferred from a successful build."""
import csv
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
matrix = ROOT / 'qa/TEST_CASES.csv'
original = ROOT / 'qa/results/baseline/TEST_CASES.csv'
if not original.exists():
    original.write_bytes(matrix.read_bytes())
rows = list(csv.DictReader(original.open(encoding='utf-8-sig', newline='')))
checks = {}
def put(ids, status, evidence, notes):
    for id in ids.split():
        checks[id] = status, evidence, notes

put('TC-BOOT-01', 'PASS', 'qa/results/reproducibility.json', 'Dois clean builds; bytes idênticos; dependências reais, sem stubs.')
put('TC-BOOT-02 TC-BOOT-06 TC-BOOT-08', 'PASS', 'qa/results/isolated-48-final.log', 'Cliente de produção e servidor integrado; found/force/status, permissões e reabertura.')
put('TC-BOOT-03', 'NOT RUN', 'qa/results/server-alpha6.log', 'Servidor dedicado abriu mundo; conexão de cliente ao dedicado não executada.')
put('TC-BOOT-04 TC-SAVE-01', 'BLOCKED', 'qa/results/final-clean-build.log', 'Novo mundo e fixtures Properties v2 testados; nenhum mundo Alpha 1–5 fornecido. Não comprova migração histórica.')
put('TC-BOOT-05', 'PASS', 'qa/results/final-clean-build.log', 'Alvos reais carregados com mixins obrigatórios aplicados; geração natural ainda não exercitada.')
put('TC-BOOT-07 TC-DATA-03', 'NOT RUN', 'qa/results/final-clean-build.log', 'Ofertas reais consultadas nos cinco níveis; UI de comércio + /reload de datapacks não executados.')
put('TC-BOOT-09', 'PASS', 'qa/results/fo-48-final.log', 'Recarga real dos recursos do cliente no pacote FO e posterior comparação das identidades.')
put('TC-SPAWN-01 TC-SPAWN-02 TC-SPAWN-03 TC-SPAWN-05 TC-SPAWN-08', 'PASS', 'qa/results/isolated-48-final.log', '48 NPCs, 24 por gênero; ADULT no cliente; identidade, diversidade e unload efetivo conferidos por UUID.')
put('TC-SPAWN-04 TC-SPAWN-11 TC-SPAWN-12', 'PASS', 'qa/results/final-clean-build.log', 'GameTests reais: Age 30/40, summons livres, genética SIZE preservada em reparo parcial.')
put('TC-SPAWN-06', 'PASS', 'qa/results/fo-full-process-restart.log', '48 identidades e crew conferidas em novo processo: PID 30728 -> 11556, mesmo UUID do jogador. Primeiro mundo salvou/fechou; JVM anterior exigiu término pelo auditor por hang no FO. Segunda JVM encerrou normalmente.')
put('TC-SPAWN-07', 'PASS', 'qa/results/isolated-48-final.log', 'Metadados em checkpoints inicial/30/120s e após reload; texturas compostas >500 pixels opacos; screenshots arquivadas.')
put('TC-SPAWN-09', 'PASS', 'qa/results/final-clean-build.log', 'Roupa MCA válida e bloqueada preservada após ticks de reparo.')
put('TC-SPAWN-10', 'NOT RUN', 'qa/results/final-clean-build.log', 'Rota summon e classe Seapost testadas; não foi localizado/gerado seapost natural como cenário de validação.')
put('TC-AI-01 TC-AI-04 TC-AI-07 TC-AI-09', 'PASS', 'qa/results/final-clean-build.log', 'GameTests: IA ativa contra três monstros; golpe 1000, Fire Aspect/efeitos, separação por contramestre e bloqueio de fogo amigo.')
put('TC-AI-05', 'NOT RUN', 'qa/results/final-clean-build.log', 'Queda externa letal e fogo/efeito externos liberados; matriz completa com jogador/mob durante duelo não executada.')
put('TC-AI-02 TC-AI-03 TC-AI-06 TC-AI-08 TC-AI-10', 'NOT RUN', 'qa/results/final-clean-build.log', 'Há verificações parciais de guarda, roupas comuns, agressão do capitão e unload; não cobrem integralmente este cenário.')
put('TC-CREW-01 TC-CREW-02 TC-CREW-03 TC-CREW-06 TC-CREW-07', 'PASS', 'qa/results/isolated-48-final.log', 'Comandos reais sem OP e administrativos; duas crews distintas preservadas, cargos exclusivos e crew após reabertura.')
put('TC-CREW-04', 'PASS', 'qa/results/final-clean-build.log', 'GameTest de claim: capitão ativo recusa; vaga sem hearts recusa; 40 hearts em metade de dois adultos aceita.')
put('TC-CREW-05', 'NOT RUN', 'qa/results/isolated-48-final.log', 'Summons e crews existentes não fundem; dois seaposts naturais próximos não testados.')
put('TC-CREW-08 TC-CREW-09', 'BLOCKED', '', 'Contrato incompleto: ATTACK sem alvo explícito; HOLD sem âncora; imediato não possui gestão interina completa.')
put('TC-CREW-10', 'NOT RUN', 'qa/results/isolated-48-final.log', 'Renúncia real e sucessão passaram; rendição durante motim real ainda não executada.')
put('TC-CREW-11 TC-SOC-01 TC-SOC-02 TC-SOC-03 TC-SOC-05 TC-SOC-08 TC-SOC-09', 'PASS', 'qa/results/final-clean-build.log', 'Regressões/JUnit de invariantes, limiares, conspiração, pausa offline, testemunhas direcionais e 128 eventos. Não equivale a motim completo em jogo.')
put('TC-SOC-04 TC-SOC-06', 'NOT RUN', 'qa/results/final-clean-build.log', 'Relógio e eleição testados em ledger; não executado ultimato/combate/rendição ponta a ponta com jogador conectado.')
put('TC-SOC-07 TC-SOC-10', 'BLOCKED', '', 'Testemunho local implementado parcialmente; rumores, causalidade estruturada completa e diplomacia não implementados.')
put('TC-DATA-01', 'PASS', 'qa/results/final-clean-build.log', 'Auditoria de 20 ofertas/5 sets e consulta runtime dos cinco níveis com ofertas não vazias.')
put('TC-DATA-02', 'NOT RUN', 'qa/results/final-clean-build.log', 'Profissão própria e filtro de roupas comuns verificados; comércio comparativo com fisherman não executado.')
put('TC-SAVE-02', 'NOT RUN', 'qa/results/final-clean-build.log', 'Falha de escrita injetada preserva snapshot e permite retry; kill do processo durante transferência não executado.')
put('TC-SAVE-03', 'PASS', 'qa/results/isolated-48-final.log', 'NPC vai ao Nether e retorna mantendo identidade/crew; comando force preserva duas crews distintas.')
put('TC-SAVE-04', 'PASS', 'qa/results/final-clean-build.log;qa/results/isolated-48-final.log', 'Unload libera referência; servidor integrado fecha/reabre no mesmo processo e reconstitui somente dados do mundo.')
put('TC-PERF-01 TC-PERF-02', 'NOT RUN', '', 'Não concluídos os perfis de 10 min com 4/12/24/48, baseline sem addon, p95/CPU/heap/pathfinding ou ciclos repetidos para retenção. Spawn NoAI de 48 não é profiling.')
put('TC-COMPAT-01', 'BLOCKED', 'qa/results/fo-48-final.log;qa/results/fo-shutdown-observation.json', 'Roteiro funcional de 48 passou no FO oficial + Loader 0.20.1 + Friends & Foes/ResourcefulLib; JVM travou ao encerrar e foi terminada após >120s. Causa não estabelecida; não certificar compatibilidade completa.')

for row in rows:
    status, evidence, notes = checks.get(row['tc_id'], ('NOT RUN', '', 'Cenário não executado.'))
    row.update(status=status, evidence=evidence, notes=notes, commit='sem Git/remote')
with matrix.open('w', encoding='utf-8', newline='') as out:
    writer = csv.DictWriter(out, fieldnames=rows[0].keys())
    writer.writeheader(); writer.writerows(rows)
print(dict(Counter(row['status'] for row in rows)))
