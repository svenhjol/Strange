package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.ServerWorldInitCallback;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runestones.RunePlateItem;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, client = RunePortalsClient.class)
public class RunePortals extends CharmModule {
    public static PortalFrameBlock PORTAL_FRAME_BLOCK;

    public static final Identifier RUNE_PORTAL_BLOCK_ID = new Identifier(Strange.MOD_ID, "rune_portal");
    public static RunePortalBlock RUNE_PORTAL_BLOCK;
    public static BlockEntityType<RunePortalBlockEntity> RUNE_PORTAL_BLOCK_ENTITY;

    public static final Identifier MSG_SERVER_CREATE_PORTAL = new Identifier(Strange.MOD_ID, "server_create_portal");
    private static final Map<RegistryKey<World>, RunePortalManager> managers = new HashMap<>();

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

    public static Optional<RunePortalManager> getManager(ServerWorld world) {
        RegistryKey<World> registryKey = world.getRegistryKey();
        return managers.get(registryKey) != null ? Optional.of(managers.get(registryKey)) : Optional.empty();
    }

    public static boolean tryActivate(ServerWorld world, BlockPos pos, BlockState state) {
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
                if (world.getBlockState(pos.east().up(i)).getBlock() instanceof PortalFrameBlock
                    || world.getBlockState(pos.west().up(i)).getBlock() instanceof PortalFrameBlock) {
                    axis = Direction.Axis.X;
                    break;
                } else if (world.getBlockState(pos.north().up(i)).getBlock() instanceof PortalFrameBlock
                    || world.getBlockState(pos.south().up(i)).getBlock() instanceof PortalFrameBlock) {
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
                        BlockPos p = pos.up(y).offset(d, x);
                        if (world.getBlockState(p.up(1)).getBlock() instanceof PortalFrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.down(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).west()).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).east()).getBlock())
                            && world.getBlockState(p.down(3)).getBlock() instanceof PortalFrameBlock
                        ) {
                            start = p.down(3);
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
                        BlockPos p = pos.up(y).offset(d, z);
                        if (world.getBlockState(p.up(1)).getBlock() instanceof PortalFrameBlock
                            && validAir.contains(world.getBlockState(p).getBlock())
                            && validAir.contains(world.getBlockState(p.down(1)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2)).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).north()).getBlock())
                            && validAir.contains(world.getBlockState(p.down(2).south()).getBlock())
                            && world.getBlockState(p.down(3)).getBlock() instanceof PortalFrameBlock
                        ) {
                            start = p.down(3);
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
                final BlockState eastState = world.getBlockState(start.east(2).up(1));
                final BlockState westState = world.getBlockState(start.west(2).up(1));

                if (!(eastState.getBlock() instanceof PortalFrameBlock) || !(westState.getBlock() instanceof PortalFrameBlock))
                    return false;

                if (eastState.get(PortalFrameBlock.FACING) == Direction.NORTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).up(i + 1), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i).up(4), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).up(3 - i), order, Direction.NORTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i), order, Direction.NORTH)) return false;
                    }
                } else if (westState.get(PortalFrameBlock.FACING) == Direction.SOUTH) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(2).up(i + 1), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.west(1 - i).up(4), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(2).up(3 - i), order, Direction.SOUTH)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.east(1 - i), order, Direction.SOUTH)) return false;
                    }
                }

                break;

            case Z:
                final BlockState northState = world.getBlockState(start.north(2).up(1));
                final BlockState southState = world.getBlockState(start.south(2).up(1));

                if (!(northState.getBlock() instanceof PortalFrameBlock) || !(southState.getBlock() instanceof PortalFrameBlock))
                    return false;

                if (northState.get(PortalFrameBlock.FACING) == Direction.WEST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).up(i + 1), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(1 - i).up(4), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).up(3 - i), order, Direction.WEST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i), order, Direction.WEST)) return false;
                    }
                } else if (southState.get(PortalFrameBlock.FACING) == Direction.EAST) {
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(2).up(i + 1), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.south(1 - i).up(4), order, Direction.EAST)) return false;
                    }
                    for (int i = 0; i < 3; i++) {
                        if (!addOrder(world, start.north(2).up(3 - i), order, Direction.EAST)) return false;
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
                Charm.LOG.debug("Rune order: " + order);
                manager.createPortal(order, start, axis);
                return true;
            } else {
                Charm.LOG.debug("Could not determine portal runes");
            }
        }

        return false;
    }

    private static boolean addOrder(ServerWorld world, BlockPos pos, List<Integer> order, Direction expectedFacing) {
        final BlockState s = world.getBlockState(pos);
        if (!(s.getBlock() instanceof PortalFrameBlock))
            return false;

        if (s.get(PortalFrameBlock.FACING) != expectedFacing)
            return false;

        order.add(s.get(PortalFrameBlock.RUNE));
        return true;
    }

    private void handleServerCreatePortal(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        BlockPos pos = BlockPos.fromLong(data.readLong());
        Hand hand = data.readEnumConstant(Hand.class);
        Direction side = data.readEnumConstant(Direction.class);
        server.execute(() -> {
            World world = player.world;
            ItemStack held = player.getStackInHand(hand);
            if (!(held.getItem() instanceof RunePlateItem plate))
                return;

            BlockState state = RunePortals.PORTAL_FRAME_BLOCK.getDefaultState()
                .with(PortalFrameBlock.FACING, side)
                .with(PortalFrameBlock.RUNE, plate.getRuneValue());

            world.setBlockState(pos, state, 2);
            world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.8F, 1.0F);

            if (!player.getAbilities().creativeMode)
                held.decrement(1);

            RunePortals.tryActivate((ServerWorld)world, pos, state);
        });
    }

    private boolean handleBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(state.getBlock() instanceof PortalFrameBlock))
            return true;

        int runeValue = state.get(PortalFrameBlock.RUNE);
        ItemStack drop = new ItemStack(Runestones.RUNE_PLATES.get(runeValue));
        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), drop));

        return true;
    }

    private ActionResult handleUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        BlockPos hitPos = hit.getBlockPos();
        ItemStack held = player.getStackInHand(hand);
        BlockState state = world.getBlockState(hitPos);
        Block block = state.getBlock();

        boolean isFrameBlock = block.equals(RunePortals.PORTAL_FRAME_BLOCK);

        if (isFrameBlock && hand == Hand.MAIN_HAND && held.isEmpty()) {
            int runeValue = state.get(PortalFrameBlock.RUNE);

            if (!world.isClient) {
                if (!player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNE_PLATES.get(runeValue)));
            }

            world.setBlockState(hitPos, Blocks.CRYING_OBSIDIAN.getDefaultState(), 3);
            return ActionResult.success(world.isClient);
        }

        if (held.getItem() instanceof RunePlateItem plate) {
            Direction hitFacing = hit.getSide();
            Direction wasFacing = null;

            if (hitFacing == Direction.UP || hitFacing == Direction.DOWN)
                return ActionResult.PASS;

            if (isFrameBlock) {
                // if there's already a rune in the frame, drop it for the player
                // and note the direction that it is facing so we can reuse this side
                int runeValue = state.get(PortalFrameBlock.RUNE);
                wasFacing = state.get(PortalFrameBlock.FACING);

                if (!world.isClient && !player.isCreative())
                    PlayerHelper.addOrDropStack(player, new ItemStack(Runestones.RUNE_PLATES.get(runeValue)));

            } else if (!block.equals(Blocks.CRYING_OBSIDIAN)) {
                // the block is not a valid frameblock or crying obsidian, skip
                return ActionResult.PASS;
            }

            int runeValue = plate.getRuneValue();
            BlockState newState = RunePortals.PORTAL_FRAME_BLOCK.getDefaultState()
                .with(PortalFrameBlock.FACING, wasFacing == null ? hitFacing : wasFacing)
                .with(PortalFrameBlock.RUNE, runeValue);

            world.setBlockState(hitPos, newState, 3);
            world.playSound(null, hitPos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 0.8F, 1.0F);

            if (!world.isClient) {
                if (!player.getAbilities().creativeMode)
                    held.decrement(1);

                RunePortals.tryActivate((ServerWorld) world, hitPos, newState);
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    private void loadRunePortalManager(ServerWorld serverWorld) {
        PersistentStateManager stateManager = serverWorld.getPersistentStateManager();
        RunePortalManager manager = stateManager.getOrCreate(
            (nbt) -> RunePortalManager.fromNbt(serverWorld, nbt),
            () -> new RunePortalManager(serverWorld),
            RunePortalManager.nameFor(serverWorld.getDimension()));

        managers.put(serverWorld.getRegistryKey(), manager);
        Charm.LOG.info("[RunePortals] Loaded rune portal state manager for world " + serverWorld.getRegistryKey().getValue());
    }
}
