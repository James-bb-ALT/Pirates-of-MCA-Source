# Fase 4 — identidade das 20 skins

S1/S2/S1B: o binding de cada PNG já estava correto. A ClothingLayer desenhava a roupa padrão de profissão por cima da skin completa, uniformizando as vinte aparências. AtelierClothingMixin suprime somente a roupa piratesmca:skins/clothing quando o NPC usa uma das skins completas Atelier. A escolha de roupa MCA de outro namespace e os dados persistidos são preservados.

A segunda camada frontal dos próprios atlases tinha pixels opacos sobre os olhos. AtelierSkinDetailsLayer mantém os UVs originais e ajusta a casca de headwear: espessura 0,02 unidade e abertura frontal acima de y=-6,1. Não repinta os PNGs nem substitui cabelo, face ou genética de NPCs MCA comuns. Os lados e a parte traseira da segunda camada continuam desenhados.

PASS de binding no cliente real: todas as vinte skins foram selecionadas e renderizadas em frente/lado/costas e close de cabeça, com assertiva de ClothingLayer=null para a roupa automática do addon. A inspeção visual prioritária confirmou boatswain com uniforme azul próprio, rosto visível e ausência da roupa vermelha universal. As capturas vestidas para todas as skins fazem parte da execução de catálogo ainda pendente; consultar validation.json para sua conclusão.

PASS de preservação: todos os PNGs presentes no JAR 11.0.0-alpha.15 permanecem byte a byte idênticos no candidato atual. Nenhum arquivo antigo de runtime foi removido. preservation.json contém o hash do JAR verificado e o vínculo com o backend previamente testado.

Limite: skins completas possuem rosto/cabelo embutidos; não se declara que reproduzem todas as expressões das camadas nativas MCA. A alternativa de aparência nativa permanece disponível. As capturas e assertivas por skin não equivalem à aprovação visual automática de cada pixel.

Fechamento: as vinte skins também foram capturadas vestidas em três vistas. O catálogo completo e os hashes estão em validation.json/capture-manifest.json; a indicação de execução pendente acima era o estado anterior ao gate final.
