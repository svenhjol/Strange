package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

import javax.annotation.Nullable;

public class RunestoneBlock extends CharmBlockWithEntity {
    private final IRunestoneMaterial material;

    public RunestoneBlock(CharmModule module, IRunestoneMaterial material) {
        super(module, material.getSerializedName() + "_runestone", material.getProperties());
        this.material = material;
    }

    public IRunestoneMaterial getMaterial() {
        return material;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunestoneBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        RunestoneBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null) {
            ItemStack stack = blockEntity.getItem(0);
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5D, pos.getZ(), stack);
            level.addFreshEntity(itemEntity);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    public RunestoneBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RunestoneBlockEntity) {
            return (RunestoneBlockEntity) blockEntity;
        }
        return null;
    }
}
