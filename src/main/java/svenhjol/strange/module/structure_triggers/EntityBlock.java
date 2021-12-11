package svenhjol.strange.module.structure_triggers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;

public class EntityBlock extends CharmBlockWithEntity {
    public static final VoxelShape SHAPE;

    protected EntityBlock(CharmModule module) {
        super(module, "entity_block", Properties.copy(Blocks.GLASS).noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EntityBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        // no
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof EntityBlockEntity blockEntity) {
                NetworkHelper.sendPacketToClient((ServerPlayer) player, StructureTriggers.MSG_CLIENT_OPEN_ENTITY_BLOCK_SCREEN, buf -> {
                    buf.writeBlockPos(pos);
                });

                blockEntity.syncToClient();
            }
        }

        return InteractionResult.CONSUME;
    }

    static {
        SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    }
}
