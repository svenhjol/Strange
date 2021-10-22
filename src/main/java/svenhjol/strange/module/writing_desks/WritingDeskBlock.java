package svenhjol.strange.module.writing_desks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlock;
import svenhjol.charm.loader.CharmModule;

public class WritingDeskBlock extends CharmBlock {
    public static final DirectionProperty FACING;
    private static final Component CONTAINER_TITLE;

    public WritingDeskBlock(CharmModule module) {
        super(module, WritingDesks.WRITING_DESK_BLOCK_ID.getPath(), Properties.copy(Blocks.CARTOGRAPHY_TABLE));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            player.openMenu(state.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((syncId, playerInventory, player)
            -> new WritingDeskMenu(syncId, playerInventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        CONTAINER_TITLE = new TranslatableComponent("container.strange.writing_desk");
    }
}
