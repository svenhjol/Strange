package svenhjol.strange.runicaltars;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;
import svenhjol.strange.runestones.RunestoneHelper;

import javax.annotation.Nullable;

public class RunicAltarBlock extends CharmBlockWithEntity {
    public RunicAltarBlock(CharmModule module) {
        super(module, RunicAltars.BLOCK_ID.getPath(), Settings.copy(Blocks.STONE));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            // ensure client has the latest rune discoveries
            RunestoneHelper.syncLearnedRunesToClient((ServerPlayerEntity)player);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RunicAltarBlockEntity) {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
//                player.openHandledScreen((RunicAltarBlockEntity)blockEntity);
            }
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new RunicAltarBlockEntity();
    }
}
