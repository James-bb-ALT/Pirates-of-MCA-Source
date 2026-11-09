package dev.arandu.piratesmca;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class PirateFlagBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PirateFlagBlock> CODEC=simpleCodec(PirateFlagBlock::new);
    public PirateFlagBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        VoxelShape cloth=switch(state.getValue(FACING)) {
            case SOUTH->Block.box(8,7,7,16,16,9);case EAST->Block.box(7,7,0,9,16,8);
            case WEST->Block.box(7,7,8,9,16,16);default->Block.box(0,7,7,8,16,9);
        };
        return Shapes.or(Block.box(7,0,7,9,16,9),cloth);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(player instanceof ServerPlayer serverPlayer)PirateFlag.open(serverPlayer,pos);
        return InteractionResult.SUCCESS;
    }
}
