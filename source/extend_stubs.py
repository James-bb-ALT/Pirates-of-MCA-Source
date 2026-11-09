from pathlib import Path
p=Path(__file__).parent/'stubs/src'
src={
'net/minecraft/core/BlockPos.java':'''package net.minecraft.core; public class BlockPos {public int getX(){return 0;}public int getY(){return 0;}public int getZ(){return 0;} }''',
'net/minecraft/world/entity/EntityType.java':'''package net.minecraft.world.entity; public class EntityType<T extends Entity> {}''',
'net/minecraft/world/entity/ai/Brain.java':'''package net.minecraft.world.entity.ai; import net.minecraft.world.entity.ai.memory.MemoryModuleType; public class Brain<E extends net.minecraft.world.entity.LivingEntity> {public void eraseMemory(MemoryModuleType<?> t){} }''',
'net/minecraft/world/entity/ai/memory/MemoryModuleType.java':'''package net.minecraft.world.entity.ai.memory; public class MemoryModuleType<T> {public static final MemoryModuleType<?> HURT_BY_ENTITY=null,ATTACK_TARGET=null;}''',
'net/minecraft/world/damagesource/DamageSource.java':'''package net.minecraft.world.damagesource; import net.minecraft.world.entity.Entity; public class DamageSource {public Entity getEntity(){return null;}}''',
'net/minecraft/world/entity/monster/Monster.java':'''package net.minecraft.world.entity.monster; public class Monster extends net.minecraft.world.entity.Mob {}''',
'net/minecraft/world/level/storage/LevelResource.java':'''package net.minecraft.world.level.storage; public class LevelResource {public static final LevelResource ROOT=new LevelResource();}''',
'net/minecraft/server/MinecraftServer.java':'''package net.minecraft.server; import java.nio.file.Path;import net.minecraft.world.level.storage.LevelResource; public class MinecraftServer {public Path getWorldPath(LevelResource key){return null;}}''',
'net/minecraft/world/level/levelgen/structure/BoundingBox.java':'''package net.minecraft.world.level.levelgen.structure;public class BoundingBox { public int minX(){return 0;}public int minY(){return 0;}public int minZ(){return 0;}}''',
'net/minecraft/world/level/levelgen/structure/StructurePiece.java':'''package net.minecraft.world.level.levelgen.structure;public class StructurePiece {public BoundingBox getBoundingBox(){return null;}}''',
'net/conczin/mca/registry/EntitiesMCA.java':'''package net.conczin.mca.registry; import net.minecraft.world.entity.EntityType; import net.conczin.mca.entity.VillagerEntityMCA;public interface EntitiesMCA {public static final EntityType<VillagerEntityMCA> MALE_VILLAGER=null,FEMALE_VILLAGER=null;}''',
'org/spongepowered/asm/mixin/injection/ModifyVariable.java':'''package org.spongepowered.asm.mixin.injection; import java.lang.annotation.*;@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface ModifyVariable {String method(); At at(); boolean argsOnly() default false; int ordinal() default -1;boolean remap() default true;}''',
'org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable.java':'''package org.spongepowered.asm.mixin.injection.callback;public class CallbackInfoReturnable<T> extends CallbackInfo{public T getReturnValue(){return null;}public void setReturnValue(T x){} }''',
'com/mojang/brigadier/exceptions/CommandSyntaxException.java':'''package com.mojang.brigadier.exceptions;public class CommandSyntaxException extends Exception {}''',
'com/mojang/brigadier/context/CommandContext.java':'''package com.mojang.brigadier.context;public class CommandContext<S> {public S getSource(){return null;}}''',
'com/mojang/brigadier/Command.java':'''package com.mojang.brigadier;import com.mojang.brigadier.context.CommandContext;import com.mojang.brigadier.exceptions.CommandSyntaxException;public interface Command<S> {int run(CommandContext<S> context)throws CommandSyntaxException;}''',
'com/mojang/brigadier/builder/ArgumentBuilder.java':'''package com.mojang.brigadier.builder; import com.mojang.brigadier.Command; public abstract class ArgumentBuilder<S,T extends ArgumentBuilder<S,T>> {public T then(ArgumentBuilder<S,?> child){return null;} public T executes(Command<S> command){return null;} public T requires(java.util.function.Predicate<S> requirement){return null;} }''',
'com/mojang/brigadier/builder/LiteralArgumentBuilder.java':'''package com.mojang.brigadier.builder; public class LiteralArgumentBuilder<S> extends ArgumentBuilder<S,LiteralArgumentBuilder<S>> {}''',
'com/mojang/brigadier/tree/LiteralCommandNode.java':'''package com.mojang.brigadier.tree; public class LiteralCommandNode<S> {}''',
'com/mojang/brigadier/CommandDispatcher.java':'''package com.mojang.brigadier; import com.mojang.brigadier.builder.LiteralArgumentBuilder; import com.mojang.brigadier.tree.LiteralCommandNode; public class CommandDispatcher<S> { public LiteralCommandNode<S> register(LiteralArgumentBuilder<S> b){return null;} }''',
'net/minecraft/commands/CommandSourceStack.java':'''package net.minecraft.commands;import java.util.function.Supplier;import net.minecraft.server.level.ServerPlayer;import net.minecraft.network.chat.Component;import com.mojang.brigadier.exceptions.CommandSyntaxException;public class CommandSourceStack {public ServerPlayer getPlayerOrException()throws CommandSyntaxException{return null;}public void sendSuccess(Supplier<Component> msg,boolean broadcast){} public net.minecraft.server.permissions.PermissionSet permissions(){return null;} }''',
'net/minecraft/server/permissions/Permission.java':'''package net.minecraft.server.permissions; public interface Permission {}''',
'net/minecraft/server/permissions/PermissionSet.java':'''package net.minecraft.server.permissions; public interface PermissionSet { public boolean hasPermission(Permission permission); }''',
'net/minecraft/server/permissions/Permissions.java':'''package net.minecraft.server.permissions; public class Permissions {public static final Permission COMMANDS_MODERATOR=null;}''',
'net/minecraft/commands/Commands.java':'''package net.minecraft.commands; import com.mojang.brigadier.builder.LiteralArgumentBuilder; public class Commands { public enum CommandSelection { ALL, DEDICATED, INTEGRATED } public static LiteralArgumentBuilder<CommandSourceStack> literal(String name){return new LiteralArgumentBuilder<>();} }''',
'net/fabricmc/fabric/api/command/v2/CommandRegistrationCallback.java':'''package net.fabricmc.fabric.api.command.v2; import com.mojang.brigadier.CommandDispatcher; import net.minecraft.commands.CommandSourceStack; import net.minecraft.commands.CommandBuildContext; import net.minecraft.commands.Commands; import net.fabricmc.fabric.api.event.Event; public interface CommandRegistrationCallback { void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, Commands.CommandSelection environment); public static final Event<CommandRegistrationCallback> EVENT=null; }''',
'net/fabricmc/fabric/api/event/Event.java':'''package net.fabricmc.fabric.api.event; public abstract class Event<T> { public abstract void register(T listener); }''',
'net/minecraft/commands/CommandBuildContext.java':'''package net.minecraft.commands; public interface CommandBuildContext {}'''
}
for name,text in src.items():
 f=p/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text+'\n')
# old compile-only stub upgrades
(p/'net/minecraft/world/entity/Entity.java').write_text('''package net.minecraft.world.entity; import java.util.*;import net.minecraft.world.level.Level;import net.minecraft.util.RandomSource;import net.minecraft.world.phys.AABB; import net.minecraft.network.chat.Component;public class Entity {public int tickCount;public boolean addTag(String tag){return true;} public boolean removeTag(String tag){return true;}public Set<String> entityTags(){return Set.of();}public RandomSource getRandom(){return null;}public Level level(){return null;}public UUID getUUID(){return null;}public double distanceToSqr(Entity other){return 0;}public AABB getBoundingBox(){return null;}public boolean isAlive(){return true;}public EntityType<?> getType(){return null;}public Component getName(){return null;}public double getX(){return 0;}public double getY(){return 0;}public double getZ(){return 0;}}''')
(p/'net/minecraft/world/entity/LivingEntity.java').write_text('''package net.minecraft.world.entity;import net.minecraft.world.InteractionHand;import net.minecraft.world.entity.ai.Brain;public class LivingEntity extends Entity {public float getHealth(){return 20f;}public float getMaxHealth(){return 20f;}public void swing(InteractionHand hand){}public Brain<?> getBrain(){return null;}}''')
(p/'net/minecraft/world/entity/Mob.java').write_text('''package net.minecraft.world.entity;import net.minecraft.world.item.ItemStack;import net.minecraft.world.entity.ai.navigation.PathNavigation;import net.minecraft.world.entity.ai.control.LookControl;public class Mob extends LivingEntity {public void setPersistenceRequired(){}public void setItemSlot(EquipmentSlot slot,ItemStack item){}public PathNavigation getNavigation(){return null;}public LookControl getLookControl(){return null;} }''')
(p/'net/minecraft/world/entity/ai/navigation/PathNavigation.java').write_text('''package net.minecraft.world.entity.ai.navigation; import net.minecraft.world.entity.Entity;public class PathNavigation {public boolean moveTo(Entity target,double speed){return true;}public boolean moveTo(double x,double y,double z,double speed){return true;}public void stop(){} }''')
(p/'net/minecraft/world/phys/AABB.java').write_text('''package net.minecraft.world.phys;public class AABB {public AABB inflate(double d){return this;}}''')
(p/'net/minecraft/server/level/ServerLevel.java').write_text('''package net.minecraft.server.level;import net.minecraft.world.level.Level;import net.minecraft.world.entity.Entity;import net.minecraft.world.phys.AABB;import net.minecraft.server.MinecraftServer;import net.minecraft.resources.ResourceKey;import java.util.*;public class ServerLevel extends Level {public long getGameTime(){return 0;}public List<ServerPlayer> players(){return List.of();}public Entity getEntity(UUID id){return null;}public MinecraftServer getServer(){return null;}public ResourceKey<Level> dimension(){return null;}public <T extends Entity> List<T> getEntitiesOfClass(Class<T> cls,AABB area){return List.of();}}''')
(p/'net/minecraft/server/level/ServerPlayer.java').write_text('''package net.minecraft.server.level;import net.minecraft.world.entity.player.Player;import net.minecraft.network.chat.Component;public class ServerPlayer extends Player {public boolean isCreative(){return false;}public boolean isSpectator(){return false;}public void sendSystemMessage(Component txt){} }''')
(p/'net/minecraft/resources/ResourceKey.java').write_text('''package net.minecraft.resources;public class ResourceKey<T>{public static <T> ResourceKey<T> create(ResourceKey<? extends net.minecraft.core.Registry<T>> registry,Identifier key){return null;}public Identifier location(){return null;} }''')
(p/'net/minecraft/world/entity/ai/brain/dummy.txt').parent.mkdir(parents=True,exist_ok=True)
(p/'net/minecraft/world/entity/ai/brain/dummy.txt').unlink(missing_ok=True)
p2=p/'org/spongepowered/asm/mixin/injection/Inject.java';p2.write_text('''package org.spongepowered.asm.mixin.injection;import java.lang.annotation.*;@Retention(RetentionPolicy.CLASS)@Target(ElementType.METHOD)public @interface Inject{String method(); At at();boolean cancellable() default false;boolean remap() default true;int require() default -1;}''')

# Methods introduced by the v2 hierarchy runtime (compile only).
from pathlib import Path
p=Path(__file__).resolve().parent/'stubs/src'
q=p/'net/conczin/mca/entity/VillagerEntityMCA.java'
q.write_text(q.read_text().replace('public final void setProfession(VillagerProfession v){}', 'public void refreshBrain(net.minecraft.server.level.ServerLevel level){} public final void setProfession(VillagerProfession v){}'))
q=p/'net/minecraft/util/RandomSource.java'
q.write_text(q.read_text().replace('int nextInt(int bound);', 'default boolean nextBoolean(){return false;} int nextInt(int bound);'))
q=p/'net/minecraft/world/entity/ai/Brain.java'
s=q.read_text()
s=s.replace('public void eraseMemory(MemoryModuleType<?> t){}','public void eraseMemory(MemoryModuleType<?> t){} public <V> java.util.Optional<V> getMemoryInternal(MemoryModuleType<V> t){return java.util.Optional.empty();}')
q.write_text(s)
q=p/'net/minecraft/world/entity/Mob.java'
s=q.read_text().replace('public void setPersistenceRequired(){}','public net.minecraft.world.entity.ai.sensing.Sensing getSensing(){return null;} public void setPersistenceRequired(){}')
q.write_text(s)
q=p/'net/minecraft/world/entity/ai/sensing/Sensing.java'
q.parent.mkdir(parents=True,exist_ok=True)
q.write_text('package net.minecraft.world.entity.ai.sensing; import net.minecraft.world.entity.Entity; public class Sensing{public boolean hasLineOfSight(Entity entity){return true;}}')

# Visual/age APIs verified against the user-supplied MCA 8.1.11+26.2 JAR.
q=p/'net/conczin/mca/entity/VillagerEntityMCA.java'
s=q.read_text()
s=s.replace('public void refreshBrain(net.minecraft.server.level.ServerLevel level){}', """public String getSkin(){return "";} public void setSkin(String id){} public void randomizeSkin(){} public String getHairStyleId(){return "";} public void randomizeHair(){} public int getAge(){return 0;} public boolean setAgeState(net.conczin.mca.entity.ai.relationship.AgeState age){return true;} public void refreshBrain(net.minecraft.server.level.ServerLevel level){}""")
q.write_text(s)
