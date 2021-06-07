package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.ServerWorldInitCallback;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.RunePlateItem;
import svenhjol.strange.module.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, client = RunePortalsClient.class)
public class RunePortals extends CharmModule {
    public static PortalFrameBlock PORTAL_FRAME_BLOCK;

    public static final ResourceLocation RUNE_PORTAL_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "rune_portal");
    public static RunePortalBlock RUNE_PORTAL_BLOCK;
    public static BlockEntityType<RunePortalBlockEntity> RUNE_PORTAL_BLOCK_ENTITY;

    public static final ResourceLocation MSG_SERVER_CREATE_PORTAL = new ResourceLocation(Strange.MOD_ID, "server_create_portal");
    private static final Map<ResourceKey<Level>, RunePortalManager> managers = new HashMap<>();

    @Override
    public void register() {
        PORTAL_FRAME_BLOCK = new PortalFrameBlock(this);
        RUNE_PORTAL_BLOCK = new RunePortalBlock(this);
        RUNE_PORTAL_BLOCK_ENTITY = RegistryHelper.blockEntity(RUNE_PORTAL_BLOCK_ID, RunePortalBlockEntity::new, RUNE_PORTAL_BLOCK);

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_CREATE_PORTAL, this::handleServerCreatePortal);
    }

    @Override
    public void init() {
        // listen for broken frames
        PlayerBlockBreakEvents.BEFORE.register(this::handleBlockBreak);

        // listen for when player tries to add a rune to a crying obsidian block
        UseBlockCallback.EVENT.register(this::handleUseBlock);

        // load rune portal manager when world starts
        ServerWorldInitCallback.EVENT.register(this::loadRunePortalManager);
    }

    public static Optional<RunePortalManager> getManager(ServerLevel world) {
        ResourceKey<Level> registryKey = world.dimension();
        return managers.get(registryKey) != null ? Optional.of(managers.get(registryKey)) : Optional.empty();
    }

    public static boolean tryActivate(ServerLevel world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof PortalFrameBlock))
            return false;

        Direction.Axis axis = null;
        List<Integer> order = new ArrayList<>();

        if (world.getBlockState(pos.east()).getBlock() instanceof PortalFrameBlock
            || world.getBlockState(pos.west()).getBlock() instanceof PortalFrameBlock) {
            axis = Direction.Axis.X;
        } else if (world.getBlockState(pos.north()).getBlock() instanceof PortalFrameBlock
            || world.getBlockState(pos.south()).getBlock() instanceof PortalFrameBlock) {
            axis = Direction.Axis.Z;
        } else {

            // try and work out axis from row above/below
            for (int i = -3; i < 4; i++) {
                if (world.getBlockState(pos.east().above(i)).getBlock() instanceof PortalFrameBlock
                    || world.getBlockState(pos.west().above(i)).getBlock() instanceof PortalFrameBlock) {
                    axis = Direction.Axis.X;
                    break;
                } else if (world.getBlockState(pos.north().above(i)).getBlock() instanceof PortalFrameBlock
                    || world.getBlockState(pos.south().above(i)).getBlock() instanceof PortalFrameBlock) {
                    axis = Direction.Axis.Z;
                    break;
                }
            }
        }

        if (axis == null)
            return false;

        List<Block> validAir = Arrays.asList(
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            RunePortals.RUNE_PORTAL_BLOCK
        );

        // try and determine middle of bottom row
        BlockPos start = null;
        if (axis == Direction.Axis.X) {
            Direction[] directions = new Direction[]{Direction.EAST, Direction.WEST};
            for (Direction d : directions) {
                for (int y = -3; y <= 3; y++) {
                    for (int x = -3; x <= 3; x++) {
                        BlockPos p = pos.above(y).relative(d, x);
                        if (world.getBlockState(p.above(1)).getBlock() instanceof PortalFrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.below(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2).west()).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2).east()).getBlock())
                            && world.getBlockState(p.below(3)).getBlock() instanceof PortalFrameBlock
                        ) {
                            start = p.below(3);
                            break;
                        }
                    }
                }
            }
        } else {
            Direction[] directions = new Direction[]{Direction.NORTH, Direction.SOUTH};
            for (Direction d : directions) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = pos.above(y).relative(d, z);
                        if (world.getBlockState(p.above(1)).getBlock() instanceof PortalFrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.below(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2).north()).getBlock())
                            && validAir.contains(world.getBlockState(p.below(2).south()).getBlock())
                            && world.getBlockState(p.below(3)).getBlock() instanceof PortalFrameBlock
                        ) {
                            start = p.below(3);
                            break;
                        }
                    }
                }
            }
        }

        if (start == null) {
            return false;
        }

        switch (axis) {
            case X:
                final BlockState eastState = world.getBlockState(start.east(2).above(1));
                final BlockState westState = world.getBlockState(start.west(2).above(1));

                if (!(eastState.getBlock() instanceof PortalFrameBlock) || !(westState.getBlock() instanceof PortalFrameBlock))
                    return false;

                if (eastState.getValue(PortalFrameBlock.FACING) == Direction.NORTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).above(i + 1), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i).above(4), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).above(3 - i), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i), order, Direction.NORTH)) return false;
                    }
                } else if (westState.getValue(PortalFrameBlock.FACING) == Direction.SOUTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).above(i + 1), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i).above(4), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).above(3 - i), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i), order, Direction.SOUTH)) return false;
                    }
                }

                break;

            case Z:
                final BlockState northState = world.getBlockState(start.north(2).above(1));
                final BlockState southState = world.getBlockState(start.south(2).above(1));

                if (!(northState.getBlock() instanceof PortalFrameBlock) || !(southState.getBlock() instanceof PortalFrameBlock))
                    return false;

                if (northState.getValue(PortalFrameBlock.FACING) == Direction.WEST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).above(i + 1), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(1 - i).above(4), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).above(3 - i), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i), order, Direction.WEST)) return false;
                    }
                } else if (southState.getValue(PortalFrameBlock.FACING) == Direction.EAST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).above(i + 1), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i).above(4), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).above(3 - i), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(1 - i), order, Direction.EAST)) return false;
                    }
                }

                break;

            default:
                return false;
        }

        Optional<RunePortalManager> optional = RunePortals.getManager(world);

        if (optional.isPresent()) {
            RunePortalManager manager = optional.get();

            if (order.size() == 12) {
                Charm.LOG.debug("Rune order: " + order + ", start: " + start.toShortString());
                manager.createPortal(order, start, axis);
                return true;
            } else {
                Charm.LOG.debug("Could not determine portal runes");
            }
        }

        return false;
    }

    private static boolean addOrder(ServerLevel world, BlockPos pos, List<Integer> order, Direction expectedFacing) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof PortalFrameBlock))
            return false;

        if (s.getValue(PortalFrameBlock.FACING) != expectedFacing)
            return false;

        order.add(s.getValue(PortalFrameBlock.RUNE));
        return true;
    }

    private void handleServerCreatePortal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        BlockPos pos = BlockPos.of(data.readLong());
        InteractionHand hand = data.readEnum(InteractionHand.class);
        Direction side = data.readEnum(Direction.class);
        server.execute(() -> {
            Level world = player.level;
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof RunePlateItem plate))
                return;

            BlockState state = RunePortals.PORTAL_FRAME_BLOCK.defaultBlockState()
                .setValue(PortalFrameBlock.FACING, side)
                .setValue(PortalFrameBlock.RUNE, plate.getRuneValue());

            world.setBlock(pos, state, 2);
            world.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);

            if (!player.getAbilities().instabuild)
                held.shrink(1);

            RunePortals.tryActivate((ServerLevel) world, pos, state);
        });
    }

    private boolean handleBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof PortalFrameBlock))
            return true;

        int runeValue = state.getValue(PortalFrameBlock.RUNE);
        ItemStack drop = new ItemStack(Runestones.RUNE_PLATES.get(runeValue));
        world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), drop));

        return true;
    }

    private InteractionResult handleUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        BlockPos hitPos = hit.getBlockPos();
        ItemStack held = player.getItemInHand(hand);
        BlockState state = world.getBlockState(hitPos);
        Block block = state.getBlock();

        boolean isFrameBlock = block.equals(RunePortals.PORTAL_FRAME_BLOCK);

        if (isFrameBlock && hand == InteractionHand.MAIN_HAND && held.isEmpty()) {
            int runeValue = state.getValue(PortalFrameBlock.RUNE);

            if (!world.isClientSide) {
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNE_PLATES.get(runeValue)));
            }

            world.setBlock(hitPos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        if (held.getItem() instanceof RunePlateItem plate) {
            Direction hitFacing = hit.getDirection();
            Direction wasFacing = null;

            if (hitFacing == Direction.UP || hitFacing == Direction.DOWN)
                return InteractionResult.PASS;

            if (isFrameBlock) {
                // if there's already a rune in the frame, drop it for the player
                // and note the direction that it is facing so we can reuse this side
                int runeValue = state.getValue(PortalFrameBlock.RUNE);
                wasFacing = state.getValue(PortalFrameBlock.FACING);

                if (!world.isClientSide && !player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNE_PLATES.get(runeValue)));

            } else if (!block.equals(Blocks.CRYING_OBSIDIAN)) {
                // the block is not a valid frameblock or crying obsidian, skip
                return InteractionResult.PASS;
            }

            int runeValue = plate.getRuneValue();
            BlockState newState = RunePortals.PORTAL_FRAME_BLOCK.defaultBlockState()
                .setValue(PortalFrameBlock.FACING, wasFacing == null ? hitFacing : wasFacing)
                .setValue(PortalFrameBlock.RUNE, runeValue);

            world.setBlock(hitPos, newState, 3);
            world.playSound(null, hitPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);

            if (!world.isClientSide) {
                if (!player.getAbilities().instabuild)
                    held.shrink(1);

                RunePortals.tryActivate((ServerLevel) world, hitPos, newState);
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    private void loadRunePortalManager(ServerLevel serverWorld) {
        DimensionDataStorage stateManager = serverWorld.getDataStorage();
        RunePortalManager manager = stateManager.computeIfAbsent(
            (nbt) -> RunePortalManager.fromNbt(serverWorld, nbt),
            () -> new RunePortalManager(serverWorld),
            RunePortalManager.nameFor(serverWorld.dimensionType()));

        managers.put(serverWorld.dimension(), manager);
        Charm.LOG.info("[RunePortals] Loaded rune portal state manager for world " + serverWorld.dimension().location());
    }
}
