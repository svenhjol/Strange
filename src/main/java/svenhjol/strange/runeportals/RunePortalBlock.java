package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.block.CharmBlockWithEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RunePortalBlock extends CharmBlockWithEntity {
    public static final EnumProperty<Axis> AXIS = Properties.HORIZONTAL_AXIS;

    protected static final VoxelShape FACING_X = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape FACING_Z = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final Map<UUID, Long> ticksInPortal = new HashMap<>();

    public RunePortalBlock(CharmModule module) {
        super(module, "rune_portal", FabricBlockSettings.of(Material.PORTAL)
            .sounds(BlockSoundGroup.GLASS)
            .noCollision()
            .strength(-1.0F)
            .luminance(11)
            .dropsNothing());

        this.setDefaultState(this.getDefaultState().with(AXIS, Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch(state.get(AXIS)) {
            case Z:
                return FACING_Z;
            case X:
            default:
                return FACING_X;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AXIS);
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
        // nope
    }

    @Override
    public ItemGroup getItemGroup() {
        return ItemGroup.SEARCH;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals()) {
            UUID uuid = entity.getUuid();
            long worldTime = world.getTime();

            if (ticksInPortal.containsKey(uuid)) {
                if (worldTime - ticksInPortal.get(uuid) < 5) {
                    ticksInPortal.put(uuid, worldTime);
                    return;
                } else {
                    ticksInPortal.remove(uuid);
                }
            }

            RunePortalBlockEntity portal = getBlockEntity(world, pos);
            if (portal == null)
                return;

            Optional<RunePortalManager> optional = RunePortals.getManager((ServerWorld) world);
            if (!optional.isPresent())
                return;

            RunePortalManager manager = optional.get();
            if (manager.teleport(portal.runes, portal.pos, entity)) {
                ticksInPortal.put(uuid, worldTime);
                return;
            }
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RunePortalBlockEntity(pos, state);
    }

    @Nullable
    public RunePortalBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RunePortalBlockEntity))
            return null;

        return (RunePortalBlockEntity)blockEntity;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = state.get(AXIS);
        boolean bl = axis2 != axis && axis.isHorizontal();
        boolean isValidBlock = neighborState.isOf(this) || (neighborState.isOf(RunePortals.FRAME_BLOCK) && neighborState.get(FrameBlock.RUNE) != FrameBlock.NO_RUNE);

        if (!bl && !isValidBlock) {
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                RunePortalBlockEntity portal = getBlockEntity(serverWorld, pos);
                if (portal != null)
                    RunePortals.getManager(serverWorld).ifPresent(manager -> manager.removePortal(portal.runes, portal.pos));
            }
            return Blocks.AIR.getDefaultState();
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
