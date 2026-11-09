package dev.arandu.piratesmca;

import java.util.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.conczin.mca.entity.VillagerEntityMCA;

/** Minimal server-authoritative chat interface; no arbitrary client packet can grant a captaincy. */
public final class PirateCommands {
    private PirateCommands(){}
    public static void register(){
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            var root=Commands.literal("pirates");
            PirateFlag.registerCommands(root);
            PirateWearables.commands(root);
            PirateOutfits.commands(root);
            PirateDialogues.commands(root);
            root.then(Commands.literal("crew")
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("roster").executes(ctx -> roster(ctx.getSource())))
                .then(Commands.literal("recruit").then(Commands.literal("nearest").executes(ctx -> recruit(ctx.getSource())))));
            root.then(Commands.literal("captain")
                .then(Commands.literal("found").executes(ctx -> found(ctx.getSource())))
                .then(Commands.literal("force")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                    .executes(ctx -> force(ctx.getSource())))
                .then(Commands.literal("claim").executes(ctx -> claim(ctx.getSource())))
                .then(Commands.literal("resign").executes(ctx -> resign(ctx.getSource(),false)))
                .then(Commands.literal("yield").executes(ctx -> resign(ctx.getSource(),true))));
            var officer=Commands.literal("appoint").then(Commands.literal("nearest")
                    .then(Commands.literal("first_mate").executes(ctx -> officer(ctx.getSource(),CrewLedger.Role.FIRST_MATE)))
                    .then(Commands.literal("quartermaster").executes(ctx -> officer(ctx.getSource(),CrewLedger.Role.QUARTERMASTER))));
            root.then(Commands.literal("officer").then(officer));
            var orders=Commands.literal("order");
            for(CrewLedger.Order order:CrewLedger.Order.values()){
                if(order==CrewLedger.Order.NONE)continue;
                final CrewLedger.Order selected=order;
                String word=order.name().toLowerCase(Locale.ROOT);
                for(String scope:List.of("nearby","all","nearest")) {
                    var node=Commands.literal(word).executes(ctx->giveOrder(ctx.getSource(),selected,scope,null));
                    if(order==CrewLedger.Order.ATTACK)node.then(Commands.argument("target",net.minecraft.commands.arguments.EntityArgument.entity())
                        .executes(ctx->giveOrder(ctx.getSource(),selected,scope,net.minecraft.commands.arguments.EntityArgument.getEntity(ctx,"target"))));
                    orders.then(Commands.literal(scope).then(node));
                }
            }
            root.then(orders);
            root.then(Commands.literal("debug")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("crew").executes(ctx -> debug(ctx.getSource())))
                .then(Commands.literal("identity").executes(ctx -> identity(ctx.getSource()))));
            dispatcher.register(root);
        });
    }
    private static ServerPlayer who(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException{return src.getPlayerOrException();}
    private static ServerLevel world(ServerPlayer player){return (ServerLevel) player.level();}
    private static void reply(CommandSourceStack src,String msg){src.sendSuccess(() -> Component.literal("[Pirates] "+msg),false);}
    private static int status(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger.Crew c=CrewRuntime.playerCrew(level,p);
        if(c==null){VillagerEntityMCA near=CrewRuntime.nearestPirate(level,p,12);if(near!=null){
            CrewLedger.Member m=CrewRuntime.ledger(level).member(near.getUUID());if(m!=null)c=CrewRuntime.ledger(level).crew(m.crew);
        }}
        if(c==null){reply(src,"Nenhuma tripulacao proxima. Use /pirates captain found com dois piratas livres.");return 0;}
        reply(src,"Tripulacao "+c.id+" | origem="+c.anchor+" | membros="+c.members.size()+" | capitao="+c.captain+" | fase="+c.phase+" | rebeldes="+CrewRuntime.ledger(level).conspirators(c)+"/"+CrewRuntime.ledger(level).requiredRebels(c));
        return 1;
    }
    private static int roster(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger.Crew c=CrewRuntime.playerCrew(level,p);
        if(c==null){reply(src,"Voce nao comanda uma tripulacao.");return 0;}
        CrewLedger ledger=CrewRuntime.ledger(level);
        reply(src,"Membros de "+c.id+":");
        for(UUID id:c.members){CrewLedger.Member m=ledger.member(id);if(m!=null)reply(src,id+" | "+m.role+" | "+m.profile+" | lealdade "+m.loyalty+" | ressentimento "+m.resentment+" | ordem "+m.order);}
        return 1;
    }
    static List<VillagerEntityMCA> freePirates(ServerLevel level,ServerPlayer p,int radius) {
        CrewLedger ledger=CrewRuntime.ledger(level);
        List<VillagerEntityMCA> result=new ArrayList<>();
        for(VillagerEntityMCA npc:level.getEntitiesOfClass(VillagerEntityMCA.class,p.getBoundingBox().inflate(radius))){
            if(p.distanceToSqr(npc)>radius*radius)continue;
            CrewLedger.Member member=CrewRuntime.prepareMember(level,npc);
            if(ledger.isFreeSolo(member))result.add(npc);
        }
        result.sort(Comparator.comparingDouble(p::distanceToSqr));
        return result;
    }
    private static int found(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        if(CrewRuntime.playerCrew(level,p)!=null){reply(src,"Voce ja comanda uma tripulacao.");return 0;}
        List<VillagerEntityMCA> eligible=freePirates(level,p,14);
        if(eligible.size()<2){reply(src,"Encontrei "+eligible.size()+" pirata(s) livre(s) em 14 blocos. Necessarios 2 adultos com a tag pmca_seapost; aguarde o spawn concluir. Para testes com cheats: /pirates captain force.");return 0;}
        CrewLedger.Crew newCrew=foundWithMembers(level,p,eligible,"PLAYER_FOUNDED");
        if(newCrew==null){reply(src,"Os piratas deixaram de estar disponiveis; tente novamente.");return 0;}
        ledger.save();
        reply(src,"Tripulacao fundada! "+newCrew.id+" (2 membros). Use /pirates order nearby follow.");return 1;
    }
    static CrewLedger.Crew foundWithMembers(ServerLevel level,ServerPlayer p,List<VillagerEntityMCA> eligible,String origin) {
        CrewLedger ledger=CrewRuntime.ledger(level);
        if(CrewRuntime.playerCrew(level,p)!=null || eligible.size()<2)return null;
        for(int i=0;i<2;i++)if(!eligible.get(i).isAlive() || !ledger.isFreeSolo(CrewRuntime.prepareMember(level,eligible.get(i))))return null;
        if(eligible.get(0)==eligible.get(1))return null;
        CrewLedger.Crew newCrew=ledger.getOrCreate(UUID.randomUUID(),origin);
        // Only the two nearest pirates are enrolled by the survival command;
        // remaining sailors can be recruited explicitly later.
        for(int i=0;i<2;i++){
            VillagerEntityMCA npc=eligible.get(i);
            ledger.transfer(ledger.member(npc.getUUID()),newCrew);
            CrewRuntime.syncCrewTag(npc,newCrew.id);
        }
        ledger.captain(newCrew,p.getUUID(),true);return newCrew;
    }
    /** Operator-only test shortcut: merge only unassigned singles or claim one existing crew. */
    private static int force(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        // The requirement on the command node is authoritative; repeat here to fail closed.
        if(!src.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))return 0;
        if(CrewRuntime.playerCrew(level,p)!=null){reply(src,"Voce ja e capitao. Use /pirates crew status.");return 0;}
        List<VillagerEntityMCA> free=freePirates(level,p,14);
        if(!free.isEmpty()){
            CrewLedger.Crew created=ledger.getOrCreate(UUID.randomUUID(),"PLAYER_FOUNDED");
            int count=0;
            for(VillagerEntityMCA npc:free){
                CrewLedger.Member m=ledger.member(npc.getUUID());
                if(ledger.transfer(m,created)){
                    CrewRuntime.syncCrewTag(npc,created.id);
                    count++;
                }
            }
            ledger.captain(created,p.getUUID(),true);ledger.save();
            reply(src,"Comando administrativo: voce e capitao de "+count+" pirata(s) livres. Crew="+created.id);return 1;
        }
        VillagerEntityMCA nearest=CrewRuntime.nearestPirate(level,p,14);
        if(nearest==null){reply(src,"Nenhum pirata adulto identificado em 14 blocos.");return 0;}
        CrewLedger.Member m=CrewRuntime.prepareMember(level,nearest);
        CrewLedger.Crew c=m==null?null:ledger.crew(m.crew);
        if(c==null){reply(src,"O pirata ainda nao possui registro de tripulacao.");return 0;}
        ledger.captain(c,p.getUUID(),true);ledger.save();
        reply(src,"Comando administrativo: voce assumiu a tripulacao existente "+c.id+" com "+c.members.size()+" membros.");return 1;
    }
    private static int claim(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        if(CrewRuntime.playerCrew(level,p)!=null){reply(src,"Voce ja e capitao.");return 0;}
        VillagerEntityMCA npc=CrewRuntime.nearestPirate(level,p,12);
        if(npc==null){reply(src,"Nenhum pirata adulto proximo.");return 0;}
        CrewLedger.Member m=ledger.member(npc.getUUID());CrewLedger.Crew crew=m==null?null:ledger.crew(m.crew);
        if(crew==null){reply(src,"Tripulacao nao identificada.");return 0;}
        if(ledger.isFreeSolo(m)){
            reply(src,"Pirata sem tripulacao; para fundar use /pirates captain found com 2 NPCs ou /pirates captain force (administrador).");
            return 0;
        }
        if(crew.captain!=null){reply(src,"Candidatura recusada: a tripulacao ja possui capitao definitivo.");return 0;}
        int supporters=0;
        int eligible=(int)crew.members.stream().map(ledger::member)
            .filter(member->member!=null && member.alive && member.adult).count();
        for(VillagerEntityMCA other:level.getEntitiesOfClass(VillagerEntityMCA.class,p.getBoundingBox().inflate(24))){
            CrewLedger.Member om=ledger.member(other.getUUID());
            if(om==null||!om.alive||!om.adult||!crew.id.equals(om.crew)||other.isBaby()||p.distanceToSqr(other)>24*24)continue;
            if(other.getVillagerBrain().getMemoriesForPlayer(p).getHearts()>=40)supporters++;
        }
        if(eligible<2 || supporters<(eligible+1)/2){reply(src,"Candidatura recusada: exige 2 adultos e apoio de 50% (+40 hearts MCA). Apoio: "+supporters+"/"+eligible);return 0;}
        ledger.captain(crew,p.getUUID(),true);ledger.save();
        reply(src,"Voce agora e capitao da tripulacao "+crew.id);return 1;
    }
    private static int resign(CommandSourceStack src,boolean yield) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        CrewLedger.Crew crew=CrewRuntime.playerCrew(level,p);
        if(crew==null){reply(src,"Voce nao e capitao.");return 0;}
        if(yield&&crew.phase!=CrewLedger.Phase.ULTIMATUM&&crew.phase!=CrewLedger.Phase.REBELLION){reply(src,"Nao ha motim ativo; use /pirates captain resign.");return 0;}
        ledger.voluntaryDeparture(crew,level.getGameTime(),yield);CrewRuntime.stopCrewCombat(level,crew,level.getGameTime());
        CrewRuntime.promoteSuccessor(ledger,crew,level.getGameTime());ledger.save();
        reply(src,yield?"Rendicao aceita. Lideranca transferida.":"Voce renunciou. Lideranca transferida.");return 1;
    }
    private static int officer(CommandSourceStack src,CrewLedger.Role role) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        CrewLedger.Crew crew=CrewRuntime.playerCrew(level,p);if(crew==null){reply(src,"Somente o capitao pode nomear oficiais.");return 0;}
        VillagerEntityMCA npc=CrewRuntime.targetedPirate(level,p,6);
        if(npc==null||!crew.members.contains(npc.getUUID())){reply(src,"Mire em um unico membro adulto da sua tripulacao, a ate 6 blocos.");return 0;}
        ledger.officer(crew,npc.getUUID(),role);ledger.save();reply(src,"Oficial nomeado: "+role+" - "+npc.getName().getString());return 1;
    }
    private static int recruit(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);CrewLedger ledger=CrewRuntime.ledger(level);
        CrewLedger.Crew own=CrewRuntime.playerCrew(level,p);if(own==null){reply(src,"Funde uma tripulacao antes de recrutar.");return 0;}
        VillagerEntityMCA npc=CrewRuntime.targetedPirate(level,p,6);if(npc==null){reply(src,"Mire em um unico pirata adulto, a ate 6 blocos.");return 0;}
        CrewLedger.Member m=ledger.member(npc.getUUID());if(m==null||own.id.equals(m.crew)){reply(src,"NPC ja recrutado ou nao identificado.");return 0;}
        CrewLedger.Crew old=ledger.crew(m.crew);
        if(old!=null && old.members.size()>1 && npc.getVillagerBrain().getMemoriesForPlayer(p).getHearts()<40){reply(src,"Pirata de outra tripulacao exige ao menos 40 hearts de confianca.");return 0;}
        ledger.transfer(m,own);CrewRuntime.syncCrewTag(npc,own.id);ledger.save();reply(src,"Pirata recrutado: "+npc.getName().getString());return 1;
    }
    private static int giveOrder(CommandSourceStack src,CrewLedger.Order order,String selection,net.minecraft.world.entity.Entity explicit) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p=who(src);ServerLevel level=world(p);if(CrewRuntime.playerCrew(level,p)==null){reply(src,"Sem autoridade: voce nao e capitao.");return 0;}
        if(selection.equals("nearest") && CrewRuntime.targetedPirate(level,p,6)==null){reply(src,"Mire em um unico pirata adulto da sua tripulacao, a ate 6 blocos.");return 0;}
        net.minecraft.world.entity.LivingEntity target=null;
        if(order==CrewLedger.Order.ATTACK) {
            var candidate=explicit==null?CrewRuntime.targetedHostile(level,p,24):explicit;
            if(!(candidate instanceof net.minecraft.world.entity.monster.Monster monster) || !monster.isAlive() || monster.level()!=level || p.distanceToSqr(monster)>24*24 || !p.hasLineOfSight(monster)){
                reply(src,"ATTACK recusado: mire em um monstro hostil a ate 24 blocos ou informe seu UUID. Aliados e capitao nao sao alvos validos.");return 0;
            }
            target=monster;
        }
        int obeyed=CrewRuntime.issueOrder(level,p,order,selection.toUpperCase(Locale.ROOT),target);
        reply(src,"Ordem "+order+" aceita por "+obeyed+" piratas. Recusas sao registradas.");return obeyed;
    }
    private static int debug(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return status(src);
    }
    /** Read-only visual diagnostics for the nearest pirate. */
    private static int identity(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player=who(src);
        VillagerEntityMCA npc=CrewRuntime.nearestPirate(world(player),player,12);
        if(npc==null){reply(src,"Nenhum pirata adulto num raio de 12 blocos.");return 0;}
        reply(src,"NPC="+npc.getUUID()+" | type="+npc.getType()
            +" | age="+npc.getAge()+" | stage="+npc.getAgeState()
            +" | gender="+npc.getGenetics().getGender());
        reply(src,"skin="+npc.getSkin()+" | clothes="+npc.getClothes()
            +" | locked="+npc.isClothingLocked()+" | hair="+npc.getHairStyleId());
        reply(src,"head="+npc.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)+" | preset="+npc.entityTags().contains(PirateWearables.PRESET)+" | renderer=MCA/CustomHeadLayer");
        return 1;
    }
}
