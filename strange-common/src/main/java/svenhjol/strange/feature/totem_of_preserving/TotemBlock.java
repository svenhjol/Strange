package svenhjol.strange.feature.totem_of_preserving;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.Charm;
import svenhjol.charm_core.base.CharmBlockItem;
import svenhjol.charm_core.base.CharmBlockWithEntity;
import svenhjol.charm_core.base.CharmFeature;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class TotemBlock extends CharmBlockWithEntity {
    private static final VoxelShape SHAPE = Block.box(2, 2, 2, 14, 14, 14);

    public TotemBlock(CharmFeature module) {
        super(module, Properties.of(Material.AIR)
            .strength(-1.0f, 3600000.0f)
            .noOcclusion()
            .noLootTable());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TotemBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState state, boolean bl) {
        var dimension = level.dimension().location();
        if (ProtectedPositions.isProtected(dimension, pos)
            && level.getBlockEntity(pos) instanceof TotemBlockEntity totem
            && !level.isClientSide) {

            Charm.LOG.debug(getClass(), "Something wants to overwrite the totem block, emergency item drop!");
            var items = totem.getItems();
            for (var stack : items) {
                var itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                level.addFreshEntity(itemEntity);
            }
        }
        Charm.LOG.debug(getClass(), "Going to remove a totem block");
        super.onRemove(blockState, level, pos, state, bl);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player
            && !player.getLevel().isClientSide()
            && player.isAlive()
            && level.getBlockEntity(pos) instanceof TotemBlockEntity totem
            && (!TotemOfPreserving.ownerOnly || totem.getOwner().equals(player.getUUID()))
        ) {
            var serverLevel = (ServerLevel)level;
            var dimension = serverLevel.dimension().location();

            // Create a new totem item and give it to player.
            Charm.LOG.debug(getClass(), "Player has interacted with totem holder block at pos: " + pos + ", player: " + player);
            var totemItem = new ItemStack(TotemOfPreserving.ITEM.get());
            TotemItem.setItems(totemItem, totem.getItems());
            TotemItem.setMessage(totemItem, totem.getMessage());

            Charm.LOG.debug(getClass(), "Adding totem item to player's inventory: " + player);
            player.getInventory().placeItemBackInInventory(totemItem);

            // Remove the totem block.
            Charm.LOG.debug(getClass(), "Removing totem holder block and block entity: " + pos);
            ProtectedPositions.remove(dimension, pos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

        super.entityInside(state, level, pos, entity);
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

    static class BlockItem extends CharmBlockItem {
        public BlockItem(CharmFeature feature, Supplier<TotemBlock> block) {
            super(feature, block, new Item.Properties());
        }
    }
}
