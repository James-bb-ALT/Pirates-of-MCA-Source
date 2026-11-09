package dev.arandu.piratesmca;

import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

public class FlagGameTests {
    private static void expect(GameTestHelper h,boolean condition,String message){h.assertTrue(condition,Component.literal(message));}
    private static VillagerEntityMCA pirate(GameTestHelper h) {
        var npc=h.spawn(EntitiesMCA.MALE_VILLAGER,new BlockPos(2,2,2));npc.addTag(PiratesOfMCA.MARKER);npc.addTag("pmca_friendly");npc.addTag("pmca_adult_required");npc.setAge(0);npc.setNoAi(true);return npc;
    }
    @GameTest(padding=8)
    public void flagAndDialogUseRealRegistriesAndCodecs(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,1,2));h.getLevel().setBlockAndUpdate(pos,PirateFlag.BLOCK.defaultBlockState());
        var p=h.makeMockServerPlayerInLevel();p.setNoGravity(true);p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()-1.75);
        System.out.println("QA_FLAG_REACH alive="+p.isAlive()+" spectator="+p.isSpectator()+" distance="+p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)+" block="+h.getLevel().getBlockState(pos)+" eye="+p.getEyePosition()+" flag="+pos+" hit="+h.getLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getBlockPos());
        expect(h,PirateFlag.reachable(p,pos),"FLAG-01 nearby visible flag must be usable");
        var dialog=PirateFlag.dialog(p,pos);var ops=h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var encoded=Dialog.DIRECT_CODEC.encodeStart(ops,dialog).getOrThrow();
        expect(h,Dialog.DIRECT_CODEC.parse(ops,encoded).getOrThrow()!=null,"FLAG-01 native dialog must serialize to a real client");h.succeed();
    }
    @GameTest(maxTicks=80,padding=16)
    public void flagFoundationNeedsVolunteersAndCannotStealCrew(GameTestHelper h) {
        var a=pirate(h);var b=pirate(h);var p=h.makeMockServerPlayerInLevel();
        p.setNoGravity(true);
        var pos=h.absolutePos(new BlockPos(1,1,2));h.getLevel().setBlockAndUpdate(pos,PirateFlag.BLOCK.defaultBlockState());p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()-1.75);
        h.runAtTickTime(40,()->{
            var level=h.getLevel();var l=CrewRuntime.ledger(level);
            for(var n:List.of(a,b)){n.addTag("pmca_bond_v102_"+p.getUUID());n.getVillagerBrain().getMemoriesForPlayer(p).setHearts(7);}
            b.getVillagerBrain().getMemoriesForPlayer(p).setHearts(-1);
            expect(h,PirateFlag.found(p,pos)==0 && CrewRuntime.playerCrew(level,p)==null,"FLAG-02 unwilling pirate must not be recruited");
            expect(h,l.isFreeSolo(l.member(a.getUUID())) && l.isFreeSolo(l.member(b.getUUID())),"Failed founding changed crew ownership");
            b.getVillagerBrain().getMemoriesForPlayer(p).setHearts(7);
            expect(h,PirateFlag.found(p,pos)==1,"FLAG-02 two volunteers must found a crew without OP");
            var crew=CrewRuntime.playerCrew(level,p);expect(h,crew.members.size()==2 && crew.flag.equals(PirateFlag.anchor(level,pos)),"Flag and crew must be committed together");
            expect(h,PirateFlag.found(p,pos)==0,"FLAG-03 existing flag cannot found another crew");
            var stranger=h.makeMockServerPlayerInLevel();stranger.setNoGravity(true);stranger.setPos(p.getX(),p.getY(),p.getZ());
            expect(h,PirateFlag.link(stranger,pos)==0 && crew.captain.equals(p.getUUID()),"FLAG-03 stranger stole flag or captaincy");
            for(UUID id:crew.members){var m=l.member(id);m.loyalty=m.respect=m.fear=m.morale=100;}
            expect(h,CrewRuntime.issueOrder(level,p,CrewLedger.Order.RALLY,true)==2,"FLAG-04 high-authority rally must reach both members");
            for(UUID id:crew.members){var directive=l.member(id).directive;expect(h,directive.x()==pos.getX()+.5 && directive.z()==pos.getZ()+.5,"RALLY did not use flag anchor");}
            h.succeed();
        });
    }
    @GameTest(padding=8)
    public void flagRejectsDistantRemovedAndOccludedInteractions(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,1,2));h.getLevel().setBlockAndUpdate(pos,PirateFlag.BLOCK.defaultBlockState());var p=h.makeMockServerPlayerInLevel();
        p.setNoGravity(true);p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()-10);expect(h,PirateFlag.found(p,pos)==0,"FLAG-03 forged distant command was accepted");
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()-1.75);var wall=pos.offset(0,1,-1);h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
        expect(h,!PirateFlag.reachable(p,pos),"FLAG-03 wall must block flag interaction");h.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());expect(h,!PirateFlag.reachable(p,pos),"Removed flag was still usable");h.succeed();
    }
    @GameTest
    public void flagRecipeAssemblesOneRealFlag(GameTestHelper h) {
        var id=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,PirateFlag.ID);
        var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)h.getLevel().getServer().getRecipeManager().byKey(id).orElseThrow().value();
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,List.of(new ItemStack(Blocks.WOOL.pick(DyeColor.BLACK)),new ItemStack(Blocks.WOOL.pick(DyeColor.BLACK)),new ItemStack(Items.BONE),new ItemStack(Blocks.WOOL.pick(DyeColor.BLACK)),new ItemStack(Blocks.WOOL.pick(DyeColor.BLACK)),new ItemStack(Items.STICK),ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(Items.STICK)));
        expect(h,recipe.matches(input,h.getLevel()) && recipe.assemble(input).is(PirateFlag.ITEM) && recipe.assemble(input).getCount()==1,"FLAG-01 registered survival recipe does not produce one flag");h.succeed();
    }
    @GameTest(maxTicks=80,padding=16)
    public void legacyFoundCommandStillWorksWithoutFlag(GameTestHelper h) {
        var a=pirate(h);pirate(h);var p=h.makeMockServerPlayerInLevel();p.setNoGravity(true);p.setPos(a.getX()+1,a.getY(),a.getZ());
        h.runAtTickTime(40,()->{
            try {
                int result=h.getLevel().getServer().getCommands().getDispatcher().execute("pirates captain found",p.createCommandSourceStack().withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS));
                var crew=CrewRuntime.playerCrew(h.getLevel(),p);expect(h,result==1 && crew!=null && crew.members.size()==2 && crew.flag==null,"Existing survival command regressed");h.succeed();
            } catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new AssertionError(e);}
        });
    }
}
