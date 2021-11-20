package svenhjol.strange.module.dimensions.floating_islands;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.IDimension;
import svenhjol.strange.module.teleport.EntityTeleportTicket;
import svenhjol.strange.module.teleport.Teleport;

public class FloatingIslandsDimension implements IDimension {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "floating_islands");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(ID, new FloatingIslandsEffects());
        Dimensions.HORIZON_HEIGHT.put(ID, -64.0D);
    }

    @Override
    public void handleWorldTick(Level level) {
        // not required yet

    }

    @Override
    public void handleAddEntity(Entity entity) {
        // not required yet
    }

    @Override
    public void handlePlayerTick(Player player) {
        if (!player.level.isClientSide && player.level.getGameTime() % 5 == 0 && player.getY() < 0.1D) {
            // don't keep adding teleport tickets for this player
            if (Teleport.teleportTickets.stream().anyMatch(t -> t.getEntity() == player)) {
                return;
            }

            ServerLevel serverLevel = (ServerLevel)player.level;
            ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);

            if (overworld == null) {
                return;
            }

            int height = overworld.getLogicalHeight();
            BlockPos source = player.blockPosition();
            BlockPos target = new BlockPos(source.getX(), height, source.getZ());
            Teleport.teleportTickets.add(new EntityTeleportTicket(player, overworld.dimension().location(), source, target, true, true));
        }
    }
}
