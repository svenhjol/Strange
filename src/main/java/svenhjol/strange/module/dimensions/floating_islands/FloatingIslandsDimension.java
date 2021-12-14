package svenhjol.strange.module.dimensions.floating_islands;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.IDimension;
import svenhjol.strange.module.teleport.EntityTeleportTicket;
import svenhjol.strange.module.teleport.Teleport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloatingIslandsDimension implements IDimension {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "floating_islands");
    public static final List<StructureFeature<?>> STRUCTURES_TO_REMOVE;

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
    public void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        // We need to remove certain structures from generating in the Floating Islands.
        // Mineshafts generate very strangely with hanging wooden platforms.
        // Ruined portals don't allow you into the nether when built.
        // Shipwrecks, ocean ruins and monuments sometimes generate floating at Y=0 which looks very odd.
        // Woodland mansions sometimes generate over an open space, creating a huge cobblestone pillar to Y=0.
        StructureSettings settings = level.getChunkSource().getGenerator().getSettings();
        Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig = new HashMap<>(settings.structureConfig());

        for (StructureFeature<?> structureFeature : STRUCTURES_TO_REMOVE) {
            structureConfig.remove(structureFeature);
        }

        settings.structureConfig = structureConfig;
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
        // When the player falls out of the world (lower than Y=-16) then teleport them back to the overworld.
        if (!player.level.isClientSide && player.level.getGameTime() % 5 == 0 && player.getY() < -16D) {
            // don't keep adding teleport tickets for this player
            if (Teleport.teleportTickets.stream().anyMatch(t -> t.getEntity() == player)) return;

            ServerLevel serverLevel = (ServerLevel)player.level;
            ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) return;

            int height = overworld.getLogicalHeight();
            BlockPos source = player.blockPosition();
            BlockPos target = new BlockPos(source.getX(), height, source.getZ());
            Teleport.teleportTickets.add(new EntityTeleportTicket(player, overworld.dimension().location(), source, target, true, true));
        }
    }

    static {
        STRUCTURES_TO_REMOVE = Arrays.asList(
            StructureFeature.WOODLAND_MANSION,
            StructureFeature.MINESHAFT,
            StructureFeature.RUINED_PORTAL,
            StructureFeature.SHIPWRECK,
            StructureFeature.STRONGHOLD,
            StructureFeature.OCEAN_MONUMENT,
            StructureFeature.OCEAN_RUIN
        );
    }
}
