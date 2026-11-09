# Teste jogável 3.0.0-alpha.6 — 2026-11-03

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
- Dois empacotamentos produziram JAR e sources idênticos. SHA256 do JAR: `125ba247b4f2d8d020bca6695d9c28ba2510c5125301b300df02921f51a80719`.

Matriz de 62 casos: {'NOT RUN': 23, 'BLOCKED': 3, 'PASS': 36}. PASS pode ser prova de domínio/servidor;
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
