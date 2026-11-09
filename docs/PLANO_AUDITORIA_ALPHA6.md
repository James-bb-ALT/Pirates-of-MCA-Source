# Auditoria Alpha 6 — plano e limites

Contrato funcional: `ESPECIFICACAO_ORIGINAL_v2.0.md`. Validação: `01_PLANO_DE_TESTES.md`.
Origem: snapshot local Alpha 5; sem `.git`, branch, remoto ou commit fornecidos.
Licença do addon: MIT, `source/LICENCE.txt`. Dependências externas preservadas, não incorporadas ao JAR.

| Fase | Escopo / arquivos | Evidência / gate |
|---|---|---|
| 0 | Ler contratos, oito classes, resources, scripts, JARs; registrar hashes | `qa/results/baseline/`; ledger 5 PASS / 2 FAIL; auditoria 3 FAIL |
| 1 | Wrapper Gradle 9.5.1, Loom 1.17.21 não remapeado, Java 25; dependencies reais | compileJava, clean build, cliente, dedicado e GameTests; TC-BOOT |
| 2 | Identidade, visual, gênero, idade, roupa, spawn | TC-SPAWN; assert de IDs não substitui inspeção da captura |
| 3 | IA, cargos, ordens, eventos e motim | TC-AI / CREW / SOC; reprodução antes do patch |
| 4 | Migração, integração, lifecycle, desempenho e pacote | TC-SAVE / DATA / PERF / COMPAT; sem tocar saves originais |

## Auditoria funcional inicial

- `CrewLedger`: limiar inclui capitão; cargos acumulam referências; transferência não limpa oficiais; capitania sem validar vínculo/limite por jogador; serialização perde timestamps; schema desconhecido vira ledger vazio gravável. Reproduções em `LedgerSafetyRegressions`.
- `CrewRuntime`: índices globais retêm entidades e cruzam servidores; ultimato usa tempo absoluto e pode expirar offline; decisões de combate dependem de coincidências de módulo; `CEASE_FIGHT` remove apenas metade do duelo; morte ambiental não limpa roster; `ATTACK` não recebe alvo explícito; acting captain ausente.
- `PirateBehavior`: normaliza idade/gênero e corrige campos visuais; rerandomização genética baseada em campos incompletos é ambígua; perfil/humor reaplicados por tick; configuração de outfits não governa o reparo periódico; personalização de roupa bloqueada precisa de teste.
- `PirateCommands`: interface existente preservada; `nearest` não usa mira; `claim` permite depor capitão fora de motim; debug sem OP; recrutamento sem proposta contextual; faltam comandos administrativos completos.
- `PirateProfession`: profissão própria, cinco trade sets e 20 ofertas. Conferir registros e comércio no servidor real, sem inferir por JSON válido.
- Mixins MCA/Fish: assinaturas e dois call sites de seapost confrontados com `javap` dos JARs fornecidos. Aplicação em runtime e render precisam de evidência independente.

## Requisitos ainda ausentes no snapshot

Bandeira colocável, tesouraria transacional, menus MCA de recrutamento/comando, livro/HUD,
recompensas/PARDON, seleção individual por mira, alvos de ATTACK, relações diplomáticas direcionais,
rumores com deduplicação e causalidade, eventos estruturados com IDs/consequências,
eleição social completa, SavedData com codecs e migração recuperável.
Não considerar implementado apenas porque existe um campo ou enum com nome semelhante.

## Ambiente

Java Corretto 25.0.2; Minecraft 26.2; Loader 0.20.1; Fabric API 0.155.2+26.2;
MCA 8.1.11+26.2; Fish of Thieves 26.2.2.0; YACL 3.9.7+26.2-fabric.
YACL é dependência obrigatória transitiva de Fish, detectada no primeiro boot real.
Perfil local FO encontrado: pacote personalizado Vanilla Pro Max / Fabulously Optimized 2.0.2,
Minecraft 26.2 / Loader 0.20.1 / API 0.161.0+26.2. Não equivale automaticamente ao FO puro 14.1.1.
Somente cópias isoladas podem ser utilizadas em testes de compatibilidade.

Referências verificadas: https://www.fabricmc.net/2026/06/15/262.html,
https://docs.fabricmc.net/develop/loom/, https://docs.fabricmc.net/develop/automatic-testing.
