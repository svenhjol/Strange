package svenhjol.strange.module.casks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.loader.CharmModule;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings({"deprecation", "EnhancedSwitchMigration"})
public class CaskBlock extends CharmBlockWithEntity {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final VoxelShape X1, X2, X3, X4;
    public static final VoxelShape Y1, Y2, Y3, Y4;
    public static final VoxelShape Z1, Z2, Z3, Z4;
    public static final VoxelShape X_SHAPE;
    public static final VoxelShape Y_SHAPE;
    public static final VoxelShape Z_SHAPE;

    public CaskBlock(CharmModule module) {
        super(module, "cask", FabricBlockSettings.of(Material.WOOD)
            .strength(2.5F)
            .sound(SoundType.WOOD));

        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.NORTH));

        this.setBurnTime(300);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch((state.getValue(FACING)).getAxis()) {
            case X:
            default:
                return X_SHAPE;
            case Z:
                return Z_SHAPE;
            case Y:
                return Y_SHAPE;
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.getBlockEntity(pos) instanceof CaskBlockEntity cask) {
            if (!level.isClientSide) {

                if (held.getItem() == Items.NAME_TAG && held.hasCustomHoverName()) {

                    cask.name = held.getHoverName().getContents();
                    cask.setChanged();

                    playCaskNameSound(level, pos);
                    held.shrink(1);

                } else {

                    ItemStack out = cask.interact(level, pos, state, held);

                    if (out == null) {

                        // Wasn't able to process this item. Pass through.
                        playCaskInteractSound(level, pos);
                        return InteractionResult.PASS;

                    } else if (out.isEmpty()) {

                        // Added the contents to the cask, Return empty bottle.
                        playCaskFillSound(level, pos);

                        // send message to client that an item was added
                        Casks.SERVER_SEND_ADD_TO_CASK.send((ServerPlayer) player, pos);

                        // Do advancement for successful filling with potion.
                        if (cask.portions > 1 && cask.effects.size() > 1) {
                            Casks.triggerFilledWithPotion((ServerPlayer) player);
                        }

                        // Return a glass bottle to the player
                        player.getInventory().placeItemBackInInventory(new ItemStack(Items.GLASS_BOTTLE));


                    } else {

                        // Extracted contents from the cask. Return potion bottle.
                        player.getInventory().placeItemBackInInventory(out);

                        if (cask.portions > 0) {

                            // Portion extracted.
                            playCaskExtractSound(level, pos);

                        } else {

                            // The cask is empty.
                            playCaskInteractSound(level, pos);
                        }

                        // Do advancement for successful extraction of brew.
                        if (cask.portions > 1 && cask.effects.size() > 1) {
                            Casks.triggerTakenBrew((ServerPlayer) player);
                        }
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : CaskBlockEntity::serverTick;
    }

    @Override
    public CreativeModeTab getItemGroup() {
        return CreativeModeTab.TAB_DECORATIONS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        CaskBlockEntity cask = getBlockEntity(level, pos);
        if (cask != null) {
            if (itemStack.hasCustomHoverName()) {
                cask.name = itemStack.getHoverName().getContents();
            }

            // try restore contents from tag
            CompoundTag tag = itemStack.getTag();
            if (tag != null && !tag.isEmpty()) {
                cask.load(tag.getCompound(Casks.STORED_POTIONS_TAG));
            }

            if (!level.isClientSide) {
                cask.setChanged();
            }
        }

        super.setPlacedBy(level, pos, state, placer, itemStack);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CaskBlockEntity(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        CaskBlockEntity cask = this.getBlockEntity(level, pos);
        if (cask == null) return 0;
        if (cask.portions == 0) return 0;

        return Math.round((cask.portions / (float) Casks.maxPortions) * 16);
    }

    @Nullable
    public CaskBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CaskBlockEntity) {
            return (CaskBlockEntity) blockEntity;
        }

        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (random.nextInt(2) == 0) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CaskBlockEntity) {
                List<MobEffect> effects = ((CaskBlockEntity) blockEntity).effects
                    .stream()
                    .map(Registry.MOB_EFFECT::get)
                    .filter(Objects::nonNull)
                    .toList();

                effects.forEach(effect -> {
                    int color = effect.getColor();
                    double r = (double) (color >> 16 & 255) / 255.0D;
                    double g = (double) (color >> 8 & 255) / 255.0D;
                    double b = (double) (color & 255) / 255.0D;
                    level.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, (double) pos.getX() + 0.13D + (0.7D * random.nextDouble()), (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.13D + (0.7D * random.nextDouble()), r, g, b);
                });
            }
        }
    }

    private void playCaskExtractSound(Level level, BlockPos pos) {
        level.playSound(null, pos, Casks.CASK_EXTRACT_SOUND, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playCaskFillSound(Level level, BlockPos pos) {
        level.playSound(null, pos, Casks.CASK_FILL_SOUND, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playCaskInteractSound(Level level, BlockPos pos) {
        level.playSound(null, pos, Casks.CASK_INTERACT_SOUND, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void playCaskNameSound(Level level, BlockPos pos) {
        level.playSound(null, pos, Casks.CASK_NAME_SOUND, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    static {
        X1 = Block.box(1.0D, 0.0D, 4.0D, 15.0D, 16.0D, 12.0D);
        X2 = Block.box(1.0D, 1.0D, 2.0D, 15.0D, 15.0D, 14.0D);
        X3 = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        X4 = Block.box(1.0D, 4.0D, 0.0D, 15.0D, 12.0D, 16.0D);
        Y1 = Block.box(4.0D, 1.0D, 0.0D, 12.0D, 15.0D, 16.0D);
        Y2 = Block.box(2.0D, 1.0D, 1.0D, 14.0D, 15.0D, 15.0D);
        Y3 = Block.box(1.0D, 1.0D, 2.0D, 15.0D, 15.0D, 14.0D);
        Y4 = Block.box(0.0D, 1.0D, 4.0D, 16.0D, 15.0D, 12.0D);
        Z1 = Block.box(4.0D, 0.0D, 1.0D, 12.0D, 16.0D, 15.0D);
        Z2 = Block.box(2.0D, 1.0D, 1.0D, 14.0D, 15.0D, 15.0D);
        Z3 = Block.box(1.0D, 2.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        Z4 = Block.box(0.0D, 4.0D, 1.0D, 16.0D, 12.0D, 15.0D);
        X_SHAPE = Shapes.or(X1, X2, X3, X4);
        Y_SHAPE = Shapes.or(Y1, Y2, Y3, Y4);
        Z_SHAPE = Shapes.or(Z1, Z2, Z3, Z4);
    }
}
