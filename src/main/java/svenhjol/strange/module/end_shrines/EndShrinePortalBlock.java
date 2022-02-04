package svenhjol.strange.module.end_shrines;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
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

import java.util.Random;

@SuppressWarnings("deprecation")
public class EndShrinePortalBlock extends CharmBlockWithEntity {
    private static final VoxelShape SHAPE = Block.box(0.0, 6.0, 0.0, 16.0, 12.0, 16.0);

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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide
            && !entity.isPassenger()
            && !entity.isVehicle()
            && entity.canChangeDimensions()
            && entity instanceof LivingEntity livingEntity
        ) {
            var serverLevel = (ServerLevel)level;
            var portal = getBlockEntity(level, pos);
            if (portal == null) return;
            if (portal.dimension == null) return;

            // Teleport entity to spawn position in the portal's dimension.
            var spawn = serverLevel.getSharedSpawnPos();
            var destination = portal.dimension;
            var ticket = new TeleportTicket(livingEntity, destination, pos, spawn);
            ticket.allowDimensionChange(true);
            ticket.useExactPosition(false);
            Teleport.addTeleportTicket(ticket);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Nullable
    public EndShrinePortalBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EndShrinePortalBlockEntity))
            return null;

        return (EndShrinePortalBlockEntity)blockEntity;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        double x = (double)pos.getX() + random.nextDouble();
        double y = (double)pos.getY() + 0.8;
        double z = (double)pos.getZ() + random.nextDouble();

        var endShrine = getBlockEntity(level, pos);
        if (endShrine != null && endShrine.dimension != null && EndShrines.validDestinations.contains(endShrine.dimension)) {
            var index = EndShrines.validDestinations.indexOf(endShrine.dimension);
            if (index < DyeColor.values().length) {
                var dyeColor = DyeColor.byId(index);
                if (dyeColor != null) {
                    var color = dyeColor.getFireworkColor();
                    double r = (double)(color >> 16 & 0xFF) / 255.0;
                    double g = (double)(color >> 8 & 0xFF) / 255.0;
                    double b = (double)(color & 0xFF) / 255.0;
                    level.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, x, y, z, r, g, b);
                }
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return false;
    }
}
