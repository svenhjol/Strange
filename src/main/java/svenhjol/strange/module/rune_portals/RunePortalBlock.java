package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.module.CharmModule;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RunePortalBlock extends CharmBlockWithEntity {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    protected static final VoxelShape FACING_X = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape FACING_Z = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final Map<UUID, Long> ticksInPortal = new HashMap<>();

    public RunePortalBlock(CharmModule module) {
        super(module, "rune_portal", FabricBlockSettings.of(Material.PORTAL)
            .sounds(SoundType.GLASS)
            .noCollision()
            .strength(-1.0F)
            .luminance(11)
            .noDrops());

        this.registerDefaultState(this.defaultBlockState()
            .setValue(AXIS, Axis.X)
            .setValue(COLOR, DyeColor.WHITE.getId()));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(AXIS)) {
            case Z:
                return FACING_Z;
            case X:
            default:
                return FACING_X;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS, COLOR);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        // nope
    }

    @Override
    public CreativeModeTab getItemGroup() {
        return CreativeModeTab.TAB_SEARCH;
    }

    @Override
    public boolean enabled() {
        return module.enabled;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && !entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
            UUID uuid = entity.getUUID();
            long worldTime = world.getGameTime();

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

            Optional<RunePortalManager> optional = RunePortals.getManager((ServerLevel) world);
            if (!optional.isPresent())
                return;

            RunePortalManager manager = optional.get();
            if (manager.teleport(portal.runes, portal.pos, entity)) {
                ticksInPortal.put(uuid, worldTime);
                return;
            }
        }
        super.entityInside(state, world, pos, entity);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunePortalBlockEntity(pos, state);
    }

    @Nullable
    public RunePortalBlockEntity getBlockEntity(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof RunePortalBlockEntity))
            return null;

        return (RunePortalBlockEntity)blockEntity;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = state.getValue(AXIS);
        boolean bl = axis2 != axis && axis.isHorizontal();
        boolean isValidBlock = neighborState.is(this) || (neighborState.is(RunePortals.PORTAL_FRAME_BLOCK));

        if (!bl && !isValidBlock) {
            if (world instanceof ServerLevel) {
                ServerLevel serverWorld = (ServerLevel) world;
                RunePortalBlockEntity portal = getBlockEntity(serverWorld, pos);
                if (portal != null)
                    RunePortals.getManager(serverWorld).ifPresent(manager -> manager.removePortal(portal.runes, portal.pos));
            }
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }
}
