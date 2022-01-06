package svenhjol.strange.module.floating_islands_dimension;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.api.event.PlayerTickCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.teleport.Teleport;
import svenhjol.strange.module.teleport.ticket.TeleportTicket;

import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "A discoverable dimension that uses vanilla's Floating Islands world generation.")
public class FloatingIslandsDimension extends CharmModule {
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "floating_islands");
    public static final List<StructureFeature<?>> STRUCTURES_TO_REMOVE;

    public static final ResourceLocation TRIGGER_VISIT_FLOATING_ISLANDS = new ResourceLocation(Strange.MOD_ID, "visit_floating_islands");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void register() {
        Dimensions.HORIZON_HEIGHT.put(ID, -64.0D);
        addDependencyCheck(m -> Strange.LOADER.isEnabled(Dimensions.class));
    }

    @Override
    public void runWhenEnabled() {
        Dimensions.DIMENSIONS.add(ID);

        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(this::handlePlayerChangeDimension);
    }

    public void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        // We need to remove certain structures from generating in the Floating Islands.
        // Mineshafts generate very strangely with hanging wooden platforms.
        // Ruined portals don't allow you into the nether when built.
        // Shipwrecks, ocean ruins and monuments sometimes generate floating at Y=0 which looks very odd.
        // Woodland mansions sometimes generate over an open space, creating a huge cobblestone pillar to Y=0.
        if (!DimensionHelper.isDimension(level, ID)) return;
        WorldHelper.removeStructures(level, STRUCTURES_TO_REMOVE);
    }

    public void handlePlayerChangeDimension(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
        if (!DimensionHelper.isDimension(destination, ID)) return;

        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_VISIT_FLOATING_ISLANDS);
    }

    public void handlePlayerTick(Player player) {
        if (!DimensionHelper.isDimension(player.level, ID)) return;

        // When the player falls out of the world (lower than Y=-16) then teleport them back to the overworld.
        if (!Teleport.hasTeleportTicket(player)
            && !player.level.isClientSide
            && player.level.getGameTime() % 5 == 0
            && player.getY() < -16D
        ) {
            ServerLevel serverLevel = (ServerLevel)player.level;
            ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) return;

            int height = overworld.getLogicalHeight();
            BlockPos source = player.blockPosition();
            BlockPos target = new BlockPos(source.getX(), height, source.getZ());

            var ticket = new TeleportTicket(player, overworld.dimension().location(), source, target);
            ticket.useExactPosition(true);
            ticket.allowDimensionChange(true);
            Teleport.addTeleportTicket(ticket);
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
