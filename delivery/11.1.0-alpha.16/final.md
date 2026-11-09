# Pirates of MCA 11.1.0-alpha.16 — reconstrução

STATUS: PASS nos cenários executados; validação visual universal e cenários não executados permanecem abertos. Os gates de empacotamento confirmaram os quatro A/B, catálogo de 3413 novas capturas, 35 testes JVM sem falhas e preservação do backend que passou os 78 GameTests. validation.json registra o JAR exato. Não é uma certificação integral do modpack ou dos 62 casos originais.

## Escopo e causas

Contrato desta etapa: PROMPT_CODEX_RECONSTRUCAO.md, diagnóstico completo, inventário das 3333 imagens e assets reais da versão anterior. A especificação funcional original e os 62 casos de teste continuam válidos; esta execução de reconstrução visual não certifica novamente todos os recursos sociais, migrações ou desempenho.

| Achado | Causa e alteração | Evidência / limite |
|---|---|---|
| A1/A2/A3 | Equipment JSON fornecia materiais para a malha vanilla, mas os CEMs de desenvolvimento não participavam do renderer. WardrobeGeometry/WardrobeLayer agora resolvem malha própria por equipment ID e slot. | 1062 fontes correspondentes do Atelier; 29 adaptações autorais legadas identificadas; BBModels editáveis e binding em cliente real. |
| P1 | A máscara usava nomes de overlays vanilla e não os campos de roupa do MCA. Máscara ampliada; roupa/armadura recortada acima da amputação; materiais de cada prótese do traje preservados. | Capturas reais e fixtures temporais; anatomia esquerda/direita determinada pelo membro, não pela câmera. |
| P2 | O diagnóstico original era hipótese temporal: imagens paradas não confirmavam dessincronia. A integração foi auditada no bytecode real de EMF e usa a matriz completa do root e do membro efetivo após atualização de pose. | Telemetria no submit real, séries temporais, A/B de modelos wide/slim. Não se afirma reprodução retrospectiva de um defeito que a baseline estática não provou. |
| S1/S2/S1B | A skin completa estava corretamente selecionada, mas ClothingLayer sobrepunha o uniforme da profissão. Omitida somente a roupa automática do addon para essas skins. | Vinte bindings individuais, vistas nuas/vestidas e close de cabeça; PNGs originais intactos. |
| Olhos/cabelo | A casca frontal opaca da segunda camada do atlas cobria olhos. Ajustada a geometria de headwear mantendo os UVs originais. | Capturas de rosto; aparência MCA comum não é substituída. |

As malhas seguem head/body/arms/legs efetivos do renderer, incluindo escala e transformações. Armaduras customizadas cancelam a camada vanilla apenas no slot suportado. Crianças e mobs sem a camada própria mantêm fallback nativo; o controle com zumbi equipado possui assertiva explícita. Glint usa a malha completa; armor trims nativos são projetados nas superfícies principais do traje, não em cada botão/acessório decorativo.

## Execução e evidências

O resultado consolidado e o SHA256 do JAR ficam em validation.json. preservation.json verifica ausência de remoções de runtime, PNGs originais intactos e identidade byte a byte das 131 entradas de backend/dados com o candidato testado em servidor. Os 78 GameTests foram executados nesse candidato anterior; a alteração posterior é de cliente/visuais. Não são apresentados como uma nova execução integral do servidor no JAR final.

PASS de reprodução dos arquivos: tarefas jar/sourcesJar executadas novamente com classes compiladas e recursos inalterados produziram os mesmos SHA256, registrados em reproducibility.json. Isso testa a reprodução do empacotamento, não duas compilações clean independentes em máquinas diferentes.

O catálogo final é capturado novamente em Minecraft: 1091 configurações, três vistas cada, mais vinte skins nuas em três vistas/close e vestidas em três vistas, totalizando 3413 PNGs. Cada uma das 4349 peças possui log de item/equipment, geometria e material. outfit-skin-results.csv e capture-manifest.json ligam cada captura ao ZIP/JPEG e ao SHA256 do PNG original. PASS de captura/binding não significa aprovação artística universal; a coluna de revisão visual permanece explícita.

As comparações de FA+Player usam diretórios isolados, mantendo EMF/ETF, Compat Core, Compat Better Combat, Better Combat, Fresh Animations e All Extensions. A opção on/off altera somente FA+Player. UUIDs determinísticos exercitam wide e slim e o teste exige o modelo esperado. A ordem efetiva dos packs é lida em runtime; a ordem do perfil pessoal foi apenas consultada e preservada em launcher-pack-order.json.

Cada configuração A/B executa 16 fixtures MCA com três quadros por pose e nove ações de jogador com três quadros por ação. A pose é registrada durante submissão de render, usando classes e matriz efetivas, sem chamar setupAnim artificialmente. O usuário acompanhou os testes e relatou não observar problemas nos últimos três; isso é evidência visual do usuário para esses cenários, não certificação de todo o catálogo.

Blockbench Web 5.2.2 abriu os BBModels prioritários captain e fisherman com textura 128×128, contagem de cubos esperada, WebGL ativo e sem erro de página. Foi usado navegador headless isolado via Playwright, sem cliques no desktop ou perfil pessoal. Capturas/logs em blockbench-captain/ e blockbench-fisherman/.

Falhas de preparação preservadas: tentativa inicial de localizar hat no root em vez do head; cavalo selvagem desmontando a fixture; munição sobrescrita ao equipar o arco. Foram corrigidas e reexecutadas; logs de tentativas malsucedidas permanecem distintos dos logs aprovados. Não são classificados como bugs de gameplay corrigidos.

Os logs não estão isentos de mensagens ERROR: o cliente de GameTest usa sessão de teste offline e registrou HTTP 401 em serviços de perfil/Realms; a fixture também registrou opção de filtro anisotrópico 0 e ausência de classe opcional Iris. Essas mensagens não interromperam os cenários aprovados. Autenticação online, Realms, shaders/Iris e multiplayer autenticado não foram validados nesta etapa.

## Limitações e cenários não certificados

- BLOCKED: emote sem mod/trigger verificado. Primeira pessoa de gancho não foi executada nesta etapa.
- NÃO EXECUTADO: matriz completa de poses do jogador além das nove ações; profiling com 4/12/24/48 NPCs; modpack pessoal completo/FO14.1.1 e todos os seus packs; migração de mundos antigos e reinício completo com dados antigos nesta revisão.
- Fixtures MCA controladas não comprovam comportamento autônomo de mineração, combate ou navegação da IA.
- Capturas completas e UVs válidos não provam ausência de todo clipping em todas as animações, nem faces artisticamente adequadas em cada pixel. A inspeção universal permanece aberta.
- As skins completas têm rosto/cabelo embutidos e não reproduzem automaticamente todas as expressões das camadas MCA. A aparência nativa MCA continua disponível.
- IDs, atributos, armas, profissão, loot e persistência existentes foram preservados; acessórios que já eram apenas itens visuais não ganharam efeitos de gameplay nesta etapa.

## Reprodução e instalação

Java 25, Minecraft 26.2, Fabric Loader 0.20.1, Fabric API 0.155.2+26.2, MCA 8.1.11+26.2, Fish of Thieves 26.2.2.0 e Loom 1.17.21. Dependências mantidas; compilação real, sem stubs. Os dois JARs fornecidos ficam em reference-jars no workspace privado.

1. Instale Python e execute `python -m pip install -r scripts/pmca/requirements.txt`.
2. Execute `python scripts/pmca/validar_assets.py --root .` e `gradlew.bat clean build`.
3. Para reexportar fontes editadas: `python scripts/pmca/export_wardrobe.py`, depois `python scripts/pmca/validate_wardrobe.py` e novo build.

Teste de catálogo: `gradlew.bat runProductionClientTest -PatelierCatalogueCapture -PreconstructionCatalogue --daemon`.

A/B exemplo: `gradlew.bat runProductionClientTest -PreconstructionReview=fa-wide-on -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods -PclientUuid=00000000-0000-0000-0000-000000000009 --daemon`. Para off, use outro diretório de stage e acrescente `-PdisableFAPlayer`; para slim, use stage contendo slim e UUID terminado em 0001. Os packs/JARs opcionais do usuário não são redistribuídos; prepare as fixtures com suas próprias cópias. Os logs preservam os arquivos e packs efetivamente carregados.

Instale somente o JAR principal, substituindo a versão antiga. Não instale sources/gametest nem duas versões juntas. Os assets de runtime já estão embutidos. Não ative o resource pack DEMO de armadura global do Atelier.

Versionamento Major pela classificação do usuário: 11.0.0-alpha.15 → 11.1.0-alpha.16, por mudança da arquitetura de renderização. Commits são locais: o repositório não possui remoto GitHub configurado; não houve push, tag remota ou release publicada.
