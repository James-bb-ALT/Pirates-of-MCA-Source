# Gate obrigatório de compatibilidade visual

Solicitação do usuário em 2026-11-04: todos os acessórios/roupas devem ser
compatíveis com os arquivos abaixo, além da configuração padrão.
Este documento complementa os contratos originais, sem alterar seus requisitos.

1. EMF Compat Core 2.3.2 e EMF Compat Better Combat 2.1.0.
2. Fresh Animations 1.11.2, FA All Extensions 1.10.1 e FA Player 1.1.
3. Dependências reais: EMF 3.3.11, ETF 7.2.5, Better Combat 3.2.2,
   Player Animation Library 1.2.8 e Cloth Config 26.2.155.

Ordem dos packs (prioridade crescente): Fresh base, All Extensions, Player.
Preservar a configuração do perfil e copiar somente para o fixture isolado.
Não publicar packs/JARs de terceiros nos ZIPs do projeto.

Para cada asset, comparar OFF, Fresh base e stack completo acima. O gate de
cliente deve verificar os mods carregados e os packs realmente ativos.
NPCs exigem identidade, cabelo efetivo, poses e persistência; uso no jogador exige
capturas com corpo visível e ataque ativo do Better Combat. Inicialização ou
swingProgress=1 não comprovam animação de ataque. ID de cabelo não comprova a
camada efetiva do renderer MCA. Não desativar packs para declarar PASS.

```powershell
python qa/prepare_emf_extensions_fixture.py --profile 'C:/Users/victo/AppData/Roaming/ModrinthApp/profiles/Fabulously Optimized'
.\gradlew.bat runProductionClientTest -PhairReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Fixture opcional não distribuído: mods/ com os sete JARs, overrides/resourcepacks/
com os três ZIPs, overrides/options.txt com a ordem acima e overrides/config/
com as configurações EMF/ETF. O script registra hashes e não modifica o perfil.
Incluir shaders, todos os estilos em camadas, multiplayer e profiling na matriz
antes de certificação completa. Até lá registrar os limites, mesmo com smoke PASS.
