package dev.arandu.piratesmca;

import java.util.*;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Placed flag and native dialog; every action is revalidated on the server. */
public final class PirateFlag {
    private PirateFlag(){}
    public static final Identifier ID=Identifier.fromNamespaceAndPath(PiratesOfMCA.ID,"pirate_flag");
    public static PirateFlagBlock BLOCK;
    public static Item ITEM;
    public static void register() {
        if(BLOCK!=null)return;
        BLOCK=Registry.register(BuiltInRegistries.BLOCK,ID,new PirateFlagBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK,ID)).strength(1).sound(SoundType.WOOD).noCollision().noOcclusion()));
        ITEM=Registry.register(BuiltInRegistries.ITEM,ID,new BlockItem(BLOCK,new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM,ID)).useBlockDescriptionPrefix()));
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.withDefaultNamespace("functional_blocks"))).register(entries->entries.accept(ITEM));
    }
    static CrewLedger.FlagAnchor anchor(ServerLevel level,BlockPos pos){return new CrewLedger.FlagAnchor(level.dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ());}
    static boolean reachable(ServerPlayer player,BlockPos pos) {
        var level=(ServerLevel)player.level();
        return player.isAlive() && !player.isSpectator() && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36
            && level.hasChunkAt(pos) && level.getBlockState(pos).is(BLOCK)
            && level.clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,player)).getBlockPos().equals(pos);
    }
    static List<VillagerEntityMCA> volunteers(ServerLevel level,ServerPlayer player) {
        var ledger=CrewRuntime.ledger(level);var result=new ArrayList<VillagerEntityMCA>();
        for(var npc:PirateCommands.freePirates(level,player,14)) {
            var member=ledger.member(npc.getUUID());
            if(CrewRuntime.isPirate(npc) && player.hasLineOfSight(npc)
                && ledger.canVolunteer(member,npc.getVillagerBrain().getMemoriesForPlayer(player).getHearts(),npc.entityTags().contains("pmca_bond_v102_"+player.getUUID()),CrewRuntime.inBrawl(level,npc.getUUID())))result.add(npc);
        }
        return result;
    }
    private static Component text(String key,Object... args){return Component.translatable("piratesmca.flag."+key,args);}
    private static ActionButton button(String key,String command){return new ActionButton(new CommonButtonData(text(key),150),Optional.of(new StaticAction(new ClickEvent.RunCommand("/"+command))));}
    static MultiActionDialog dialog(ServerPlayer player,BlockPos pos) {
        var level=(ServerLevel)player.level();var ledger=CrewRuntime.ledger(level);
        var linked=ledger.crewAtFlag(anchor(level,pos));var own=CrewRuntime.playerCrew(level,player);
        String coordinates=pos.getX()+" "+pos.getY()+" "+pos.getZ();
        var body=new ArrayList<net.minecraft.server.dialog.body.DialogBody>();var actions=new ArrayList<ActionButton>();
        if(linked==null) {
            if(own==null) {
                int ready=volunteers(level,player).size();body.add(new PlainMessage(text("requirements",ready),300));
                if(ready>=2)actions.add(button("found","pirates flag found "+coordinates));
            } else {body.add(new PlainMessage(text("link_help"),300));actions.add(button("link","pirates flag link "+coordinates));}
        } else {
            body.add(new PlainMessage(text("members",linked.members.size()),300));
            if(own==linked) {
                body.add(new PlainMessage(text("orders_help"),300));
                for(String order:List.of("follow","hold","rally","guard","retreat","cease_fight","dismiss"))actions.add(button(order,"pirates order all "+order));
                actions.add(button("roster","pirates crew roster"));
            } else body.add(new PlainMessage(text("other_crew"),300));
        }
        actions.add(button("refresh","pirates flag open "+coordinates));
        var close=new ActionButton(new CommonButtonData(text("close"),150),Optional.empty());
        return new MultiActionDialog(new CommonDialogData(text("title"),Optional.empty(),true,false,DialogAction.CLOSE,body,List.of()),actions,Optional.of(close),2);
    }
    public static void open(ServerPlayer player,BlockPos pos){if(reachable(player,pos))player.openDialog(Holder.direct(dialog(player,pos)));}
    static int found(ServerPlayer player,BlockPos pos) {
        if(!reachable(player,pos))return denied(player,"too_far");
        var level=(ServerLevel)player.level();var ledger=CrewRuntime.ledger(level);var flag=anchor(level,pos);
        if(ledger.crewAtFlag(flag)!=null || CrewRuntime.playerCrew(level,player)!=null)return denied(player,"occupied");
        var ready=volunteers(level,player);if(ready.size()<2)return denied(player,"not_enough");
        var crew=PirateCommands.foundWithMembers(level,player,ready,"PIRATE_FLAG");
        if(crew==null)return denied(player,"not_enough");
        if(!ledger.bindFlag(crew,player.getUUID(),flag))throw new IllegalStateException("Flag claim changed during server action");
        ledger.save();player.sendSystemMessage(text("founded"));open(player,pos);return 1;
    }
    static int link(ServerPlayer player,BlockPos pos) {
        if(!reachable(player,pos))return denied(player,"too_far");
        var level=(ServerLevel)player.level();var ledger=CrewRuntime.ledger(level);var own=CrewRuntime.playerCrew(level,player);
        if(!ledger.bindFlag(own,player.getUUID(),anchor(level,pos)))return denied(player,"occupied");
        ledger.save();player.sendSystemMessage(text("linked"));open(player,pos);return 1;
    }
    private static int denied(ServerPlayer player,String reason){player.sendSystemMessage(text(reason));return 0;}
    static void registerCommands(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root) {
        var flag=Commands.literal("flag");
        for(String action:List.of("open","found","link"))flag.then(Commands.literal(action).then(Commands.argument("position",BlockPosArgument.blockPos()).executes(ctx->{
            var player=ctx.getSource().getPlayerOrException();var pos=BlockPosArgument.getLoadedBlockPos(ctx,"position");
            if(action.equals("found"))return found(player,pos);
            if(action.equals("link"))return link(player,pos);
            if(!reachable(player,pos))return denied(player,"too_far");open(player,pos);return 1;
        })));
        root.then(flag);
    }
}
