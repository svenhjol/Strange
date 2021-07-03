package svenhjol.strange.module.astrolabes;

import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.module.CharmModule;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Optional;

public class AstrolabeBlock extends CharmBlockWithEntity {
    public static final VoxelShape LEG1, LEG2, LEG3, LEG4, EDGE1, EDGE2, EDGE3, EDGE4, CENTER;
    public static final VoxelShape SHAPE;

    public AstrolabeBlock(CharmModule module) {
        super(module, "astrolabe", Properties.copy(Blocks.ENCHANTING_TABLE));
    }

    @Override
    public void createBlockItem(ResourceLocation id) {
        AstrolabeBlockItem blockItem = new AstrolabeBlockItem(this);
        RegistryHelper.item(id, blockItem);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        Optional<BlockPos> position = AstrolabeBlockItem.getPosition(itemStack);
        Optional<ResourceKey<Level>> dimension = AstrolabeBlockItem.getDimension(itemStack);
        AstrolabeBlockEntity astrolabe = getBlockEntity(world, pos);

        if (position.isPresent() && dimension.isPresent() && astrolabe != null) {
            astrolabe.dimension = dimension.get();
            astrolabe.position = position.get();
            astrolabe.setChanged();
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        AstrolabeBlockEntity astrolabe = getBlockEntity(world, pos);
        if (astrolabe != null) {
            if (!world.isClientSide) {
                ItemStack out = new ItemStack(Astrolabes.ASTROLABE);
                AstrolabeBlockItem.setDimension(out, astrolabe.dimension);
                AstrolabeBlockItem.setPosition(out, astrolabe.position);

                ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), out);
                entity.setDefaultPickUpDelay();
                world.addFreshEntity(entity);
            }
        }

        super.playerWillDestroy(world, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstrolabeBlockEntity(pos, state);
    }

    @Nullable
    public AstrolabeBlockEntity getBlockEntity(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AstrolabeBlockEntity) {
            return (AstrolabeBlockEntity) blockEntity;
        }

        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, Astrolabes.BLOCK_ENTITY, AstrolabeBlockEntity::tick);
    }

    static {
        LEG1 = Block.box(1.0D, 0.0D, 1.0D, 3.0D, 8.0D, 3.0D);
        LEG2 = Block.box(12.0D, 0.0D, 1.0D, 15.0D, 8.0D, 3.0D);
        LEG3 = Block.box(1.0D, 0.0D, 12.0D, 3.0D, 8.0D, 15.0D);
        LEG4 = Block.box(12.0D, 0.0D, 12.0D, 15.0D, 8.0D, 15.0D);
        EDGE1 = Block.box(0.0D, 9.0D, 0.0D, 16.0D, 11.0D, 3.0D);
        EDGE2 = Block.box(0.0D, 9.0D, 0.0D, 3.0D, 11.0D, 16.0D);
        EDGE3 = Block.box(0.0D, 9.0D, 13.0D, 16.0D, 11.0D, 16.0D);
        EDGE4 = Block.box(13.0D, 9.0D, 0.0D, 16.0D, 11.0D, 16.0D);
        CENTER = Block.box(4.0D, 7.0D, 4.0D, 12.0D, 14.0D, 12.0D);
        SHAPE = Shapes.or(LEG1, LEG2, LEG3, LEG4, EDGE1, EDGE2, EDGE3, EDGE4, CENTER);
    }
}
