package svenhjol.strange.module.end_shrines;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.block.CharmBlockWithEntity;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.teleport.Teleport;
import svenhjol.strange.module.teleport.ticket.TeleportTicket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class EndShrinePortalBlock extends CharmBlockWithEntity {
    private static final VoxelShape SHAPE = Block.box(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);
    private static final Map<UUID, Long> PORTAL_TICKS = new HashMap<>();

    public EndShrinePortalBlock(CharmModule module) {
        super(module, EndShrines.BLOCK_ID.getPath(), FabricBlockSettings.of(Material.PORTAL)
            .sounds(SoundType.GLASS)
            .noCollision()
            .strength(-1.0F)
            .luminance(11)
            .noDrops());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EndShrinePortalBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
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
        return module.isEnabled();
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide
            && !entity.isPassenger()
            && !entity.isVehicle()
            && entity.canChangeDimensions()
            && entity instanceof LivingEntity livingEntity
        ) {
            UUID uuid = entity.getUUID();
            long worldTime = world.getGameTime();

            if (PORTAL_TICKS.containsKey(uuid)) {
                if (worldTime - PORTAL_TICKS.get(uuid) < 5) {
                    PORTAL_TICKS.put(uuid, worldTime);
                    return;
                } else {
                    PORTAL_TICKS.remove(uuid);
                }
            }

            EndShrinePortalBlockEntity portal = getBlockEntity(world, pos);
            if (portal == null) return;
            if (portal.dimension == null) return;

            PORTAL_TICKS.put(uuid, worldTime);

            var destination = portal.dimension;
            var ticket = new TeleportTicket(livingEntity, destination, pos, pos);
            ticket.allowDimensionChange(true);
            ticket.useExactPosition(true);
            Teleport.addTeleportTicket(ticket);
        }

        super.entityInside(state, world, pos, entity);
    }

    @Nullable
    public EndShrinePortalBlockEntity getBlockEntity(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof EndShrinePortalBlockEntity))
            return null;

        return (EndShrinePortalBlockEntity)blockEntity;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        double d = (double)pos.getX() + random.nextDouble();
        double e = (double)pos.getY() + 0.8;
        double f = (double)pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return false;
    }
}
