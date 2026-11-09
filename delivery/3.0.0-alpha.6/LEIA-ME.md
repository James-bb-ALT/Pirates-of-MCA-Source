# Pirates of MCA — teste jogável 3.0.0-alpha.6

Crie um perfil separado: Minecraft 26.2, Fabric Loader 0.20.1 e Java 25.
Copie os cinco JARs desta pasta mods para o perfil. Não instale o JAR de GameTests.
Use um mundo novo com cheats para o primeiro teste e conclua a tela inicial do MCA.

Em uma área segura, invoque dois piratas adultos:
```
/summon mca:male_villager ~2 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_bruiser","pmca_gender_male","pmca_adult_required"]}
/summon mca:female_villager ~4 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}
/pirates captain found
/pirates crew status
/pirates order nearby follow
```
Aguarde cerca de um segundo após os summons. Mire em um membro a até seis blocos:
`/pirates officer appoint nearest first_mate`. Experimente `hold`, `guard` e
`cease_fight`; ATTACK exige mirar em monstro hostil a até 24 blocos. NPCs podem
recusar ordens por lealdade/risco. Para sair: `/pirates captain resign`.

Teste visual/combate, afaste-se e retorne, salve e reabra. Esta é uma versão
experimental parcial: consulte o relatório; FO, migração histórica e desempenho
completo não estão certificados. Use cópia de saves importantes.
