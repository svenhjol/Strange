package svenhjol.strange.module.cooking_pots;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.helper.ItemHelper;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeSounds;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class CookingPotBlock extends CharmBlockWithEntity {
    private static final VoxelShape RAY_TRACE_SHAPE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape OUTLINE_SHAPE;

    public static IntegerProperty LIQUID = IntegerProperty.create("liquid", 0, 2);
    public static BooleanProperty HAS_FIRE = BooleanProperty.create("has_fire");

    public CookingPotBlock(CharmModule module) {
        super(module, "cooking_pot", Properties.of(Material.METAL, MaterialColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(2.0F)
            .noOcclusion());

        this.registerDefaultState(this.defaultBlockState()
            .setValue(HAS_FIRE, false)
            .setValue(LIQUID, 0));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (state.getValue(LIQUID) == 0 && held.getItem() == Items.WATER_BUCKET) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIQUID, 1), 3);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }

                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.0F);
                CookingPots.triggerFilledWater((ServerPlayer) player);

                if (state.getValue(HAS_FIRE)) {
                    CookingPots.triggerLitFire((ServerPlayer) player);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.getValue(LIQUID) > 0 && state.getValue(HAS_FIRE)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CookingPotBlockEntity pot) {

                if (!level.isClientSide) {
                    if (held.getItem() == Items.NAME_TAG && held.hasCustomHoverName()) {
                        pot.name = held.getHoverName().getContents();
                        pot.setChanged();
                        level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.85F, 1.1F);
                        held.shrink(1);

                    } else if (held.getItem() == Items.BUCKET) {

                        // empty the pot
                        if (!player.getAbilities().instabuild) {
                            ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
                            if (held.getCount() == 1) {
                                player.setItemInHand(hand, bucket);
                            } else {
                                player.getInventory().placeItemBackInInventory(bucket);
                            }
                        }

                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);
                        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.6F);

                        pot.flush(level, pos, state);
                        pot.setChanged();

                    } else if (held.getItem() == Items.BOWL) {
                        ItemStack out = pot.take(level, pos, state, held);
                        if (out != null) {
                            PlayerHelper.addOrDropStack(player, out);

                            if (pot.getRemainingPortions() > 0) {
                                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5F, 1.0F);
                            } else {
                                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.0F);
                            }

                            // do take food advancement
                            if (pot.getRemainingPortions() > 0) {
                                CookingPots.triggerTakenFood((ServerPlayer) player);
                            }

                            level.updateNeighbourForOutputSignal(pos, CookingPots.COOKING_POT);
                        }

                    } else if (held.isEdible()) {
                        ItemStack copy = held.copy(); // for checking if it's a bowl or bottle after adding to pot
                        boolean result = pot.add(level, pos, state, held);
                        if (result) {
                            level.playSound(null, pos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 0.5F, 1.0F);

                            // if the food has a bowl, give it back to the player
                            if (ItemHelper.getBowlFoodItems().contains(copy.getItem())) {
                                PlayerHelper.addOrDropStack(player, new ItemStack(Items.BOWL));
                            }

                            // if the food has a bottle, give it back to the player
                            if (ItemHelper.getBottleFoodItems().contains(copy.getItem())) {
                                PlayerHelper.addOrDropStack(player, new ItemStack(Items.GLASS_BOTTLE));
                            }

                            // send message to client that an item was added
                            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
                            data.writeLong(pos.asLong());
                            ServerPlayNetworking.send((ServerPlayer) player, CookingPots.MSG_CLIENT_ADDED_TO_POT, data);

                            // do add items advancement
                            if (pot.getRemainingPortions() > 0) {
                                CookingPots.triggerAddedItem((ServerPlayer) player);
                            }

                            level.updateNeighbourForOutputSignal(pos, CookingPots.COOKING_POT);
                        }
                    }

                    // fire must be lit at this point so check the advancement
                    CookingPots.triggerLitFire((ServerPlayer) player);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return RAY_TRACE_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, CookingPots.BLOCK_ENTITY, CookingPotBlockEntity::tick);
    }

    @Override
    public CreativeModeTab getItemGroup() {
        return CreativeModeTab.TAB_DECORATIONS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIQUID, HAS_FIRE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        CookingPotBlockEntity pot = this.getBlockEntity(level, pos);
        if (pot == null) return 0;
        if (pot.getRemainingPortions() == 0) return 0;

        return Math.min(16, pot.getRemainingPortions());
    }

    @Nullable
    public CookingPotBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CookingPotBlockEntity) {
            return (CookingPotBlockEntity) blockEntity;
        }

        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(HAS_FIRE) && state.getValue(LIQUID) > 0 && random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.13D + (0.7D * random.nextDouble()), (double)pos.getY() + 0.92D, (double)pos.getZ() + 0.13D + (0.7D * random.nextDouble()), 0.0D, 0.0D, 0.0D);

            if (random.nextInt(30) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, StrangeSounds.COOKING_POT, SoundSource.BLOCKS, 0.25F + (0.25F * random.nextFloat()), random.nextFloat() * 0.7F + 0.4F, false);
            }

            if (random.nextInt(2) == 0) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof CookingPotBlockEntity pot) {
                    List<MobEffect> effects = pot.effects
                        .stream()
                        .map(MobEffectInstance::getEffect)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                    effects.forEach(effect -> {
                        int color = effect.getColor();
                        double r = (double) (color >> 16 & 255) / 255.0D;
                        double g = (double) (color >> 8 & 255) / 255.0D;
                        double b = (double) (color & 255) / 255.0D;
                        level.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, (double) pos.getX() + 0.25D + (0.5D * random.nextDouble()), (double) pos.getY() + 0.65D, (double) pos.getZ() + 0.25D + (0.5D * random.nextDouble()), r, g, b);
                    });
                }
            }
        }
    }

    static {
        OUTLINE_SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), RAY_TRACE_SHAPE), BooleanOp.ONLY_FIRST);
    }
}
