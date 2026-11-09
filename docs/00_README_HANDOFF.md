# Handoff para desenvolvimento — Pirates of MCA

**Snapshot de código:** Alpha 5, exatamente como fornecido; somente documentação e testes foram adicionados fora da pasta `source/`.  
**Estado:** experimental e **NÃO validado dentro do Minecraft**.  
**Artefato binário de referência:** `baseline/PiratesMCA_Alpha5_CrewRuntime_FIX.jar`.  
**Dependências de referência fornecidas pelo usuário:** `reference-jars/mca-fabric-8.1.11+26.2.jar` e `reference-jars/fish_of_thieves-mc26.2-v26.2.2.0-fabric(3).jar`. Não altere os JARs originais.

## Conteúdo

- `source/`: código-fonte Java, resources, scripts e testes originais da Alpha 5.
- `docs/ESPECIFICACAO_ORIGINAL_v2.0.md`: requisitos aprovados pelo usuário (fonte de verdade do design).
- `docs/01_PLANO_DE_TESTES.md`: cenários de regressão e evidência obrigatória.
- `docs/03_BUGS_E_RISCOS.md`: mapa de bugs, hipóteses e defeitos já reproduzidos.
- `docs/02_PROMPT_PARA_AGENTE.md`: instruções para agente Codex/Claude/outro.
- `docs/AUDITORIA_ESTATICA_EXECUCAO.txt` e `docs/TESTES_LEDGER_EXECUCAO.txt`: resultados REAIS sobre o baseline (incluem falhas).
- `qa/static_audit.py`: validação limitada da estrutura, resources e contrato de build.
- `qa/LedgerRegressions.java` e `qa/run_ledger_regressions.sh`: sete testes executáveis da camada pura de domínio; dois falham no baseline.
- `qa/TEST_RUN_TEMPLATE.md`: template para evidências de qualquer execução nova.

## Primeiras ações (Linux/macOS)

```bash
python3 qa/static_audit.py        # esperado: FAIL em 3 verificações de build legado
bash qa/run_ledger_regressions.sh # esperado: 5 PASS e 2 FAIL no snapshot
```

Scripts executam testes estáticos e de domínio, **não iniciam o jogo**. A reprovação dos testes é intencional como baseline para guiar as correções.

No Windows, usar WSL ou executar comandos equivalentes com Python 3 e JDK no PATH.

## Prioridades do agente

1. Configurar build oficial Fabric Loom 26.2/Java 25 e executar cliente + servidor reais.
2. Corrigir os crashes/assinaturas, visual/gênero/idade/nome/roupas, IA e criação de tripulação.
3. Corrigir os dois testes do domínio que falham, sem alterar as expectativas para tornar o teste verde.
4. Validar a especificação v2.0 com testes de game e do modpack; concluir somente com relatório de evidência.

**Limite de confiabilidade:** este pacote NÃO prova que a Alpha 5 funciona. O JAR referência possui falhas conhecidas no domínio e o build antigo não é reprodutível contra as dependências reais.
