package svenhjol.strange.feature.waypoints;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import svenhjol.charm.Charm;
import svenhjol.charm_api.event.PlayerLoginEvent;
import svenhjol.charm_api.event.PlayerTickEvent;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Feature(mod = Charm.MOD_ID, description = "Lodestones broadcast a message to a nearby player.\n" +
    "Place a named banner on a lodestone to start broadcasting.")
public class Waypoints extends CharmFeature {
    public static final Map<UUID, String> LAST_SEEN_WAYPOINT = new HashMap<>();
    public static final int MIN_BACKOFF_TICKS = 20;
    public static final int MAX_BACKOFF_TICKS = 50;
    public static final int BROADCAST_DISTANCE = 128;
    public static Supplier<SoundEvent> BROADCAST_SOUND;
    protected int backoff = MIN_BACKOFF_TICKS;

    @Override
    public void register() {
        BROADCAST_SOUND = Strange.REGISTRY.soundEvent("waypoint_broadcast");
        WaypointsNetwork.register();
    }

    @Override
    public void runWhenEnabled() {
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
        PlayerLoginEvent.INSTANCE.handle(this::handlePlayerLogin);
    }

    private void handlePlayerLogin(Player player) {
        WaypointsNetwork.FlushWaypoint.send(player);
    }

    private void handlePlayerTick(Player player) {
        if (player.level.isClientSide() || player.level.getGameTime() % backoff != 0) return;

        var serverPlayer = (ServerPlayer)player;
        var level = serverPlayer.getLevel();
        var pos = serverPlayer.blockPosition();
        var poiManager = level.getPoiManager();
        var uuid = serverPlayer.getUUID();
        var optional = poiManager.findClosest(
            holder -> holder.is(PoiTypes.LODESTONE), pos, BROADCAST_DISTANCE, PoiManager.Occupancy.ANY);

        if (optional.isEmpty()) {
            // Remove last seen entry, send update to player to clear it on the client.
            LAST_SEEN_WAYPOINT.remove(uuid);
            WaypointsNetwork.FlushWaypoint.send(player);

            if (backoff < MAX_BACKOFF_TICKS) {
                backoff += 5;
            }
            return;
        }
        backoff = MIN_BACKOFF_TICKS;

        var lodestonePos = optional.get();

        // Try and get a banner from above the lodestone.
        if (!(level.getBlockEntity(lodestonePos.above()) instanceof BannerBlockEntity banner)) {
            LAST_SEEN_WAYPOINT.remove(uuid);
            return;
        }

        var color = banner.getBaseColor();
        var title = banner.getDisplayName();

        // Bit of a hack to get banners to display something when unnamed.
        if (title.getString().equals("block.minecraft.banner")) {
            title = TextHelper.translatable("block.minecraft." + color.getSerializedName() + "_banner");
        }

        var hash = level.dimension().location().toString() + lodestonePos.asLong() + color.getName() + title.getString();
        var lastHash = LAST_SEEN_WAYPOINT.getOrDefault(uuid, "");
        if (hash.equals(lastHash)) return; // Already seen this waypoint, return early.

        LAST_SEEN_WAYPOINT.put(uuid, hash);

        // Play sound if there is a noteblock beneath.
        var playSound = level.getBlockState(lodestonePos.below()).getBlock() == Blocks.NOTE_BLOCK;

        // Send the waypoint data to the client.
        WaypointsNetwork.WaypointInfo.send(player, title, color, playSound);
    }
}
