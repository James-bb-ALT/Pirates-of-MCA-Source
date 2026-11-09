# 2.1.0-alpha.6 — entrega de auditoria parcial

O addon agora possui build Fabric real e regressões executadas contra Minecraft 26.2,
MCA 8.1.11+26.2 e Fish of Thieves 26.2.2.0, usando Java 25 e Loader 0.20.1.
As correções estão detalhadas no changelog e no relatório de execução.

Esta versão **não satisfaz todo o contrato funcional v2.0**. Permanecem ausentes,
entre outros, a bandeira/menu de fundação, tesouraria transacional, diálogos MCA,
SavedData nativo, diplomacia, rumores completos e ordens com todos os parâmetros
previstos. Não foi concluído o profiling comparativo de dez minutos por tamanho.

O build gera um addon independente; instale também Fabric API, MCA, Fish e YACL.
Não instalar o JAR de GameTests como addon de uso normal. Não há publicação remota:
o diretório recebido não contém repositório Git nem remoto GitHub.

O arquivo Properties v2 é preservado com backups; a suíte usa fixtures sintéticas
para corrupção e cargos legados. Isso não comprova migração de mundos históricos
Alpha 1–5, que não foram fornecidos.
