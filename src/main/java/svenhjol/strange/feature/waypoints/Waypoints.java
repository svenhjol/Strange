package svenhjol.strange.feature.waypoints;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.charmony_api.event.PlayerLoginEvent;
import svenhjol.charmony_api.event.PlayerTickEvent;
import svenhjol.strange.feature.waypoints.WaypointsNetwork.WaypointInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class Waypoints extends CommonFeature {
    public static final Map<UUID, String> LAST_SEEN_WAYPOINT = new HashMap<>();
    public static final int MIN_BACKOFF_TICKS = 20;
    public static final int MAX_BACKOFF_TICKS = 50;
    public static final int BROADCAST_DISTANCE = 128;
    public static Supplier<SoundEvent> broadcastSound;
    protected int backoff = MIN_BACKOFF_TICKS;

    @Override
    public String description() {
        return "Lodestones broadcast a message to a nearby player when a banner is placed on top.";
    }

    @Override
    public void register() {
        broadcastSound = mod().registry().soundEvent("waypoint_broadcast");
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
        if (player.level().isClientSide() || player.level().getGameTime() % backoff != 0) {
            return;
        }

        var serverPlayer = (ServerPlayer)player;
        var level = (ServerLevel)serverPlayer.level();
        var pos = serverPlayer.blockPosition();
        var poiManager = level.getChunkSource().getPoiManager();
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

        var activePos = optional.get();

        // Try and get a banner from above the lodestone.
        if (!(level.getBlockEntity(activePos.above()) instanceof BannerBlockEntity banner)) {
            LAST_SEEN_WAYPOINT.remove(uuid);
            return;
        }

        var color = banner.getBaseColor();
        var title = banner.getDisplayName();

        // Bit of a hack to get banners to display something when unnamed.
        if (title.getString().equals("block.minecraft.banner")) {
            title = TextHelper.translatable("block.minecraft." + color.getSerializedName() + "_banner");
        }

        var hash = level.dimension().location().toString() + activePos.asLong() + color.getName() + title.getString();
        var lastHash = LAST_SEEN_WAYPOINT.getOrDefault(uuid, "");
        if (hash.equals(lastHash)) return; // Already seen this waypoint, return early.

        LAST_SEEN_WAYPOINT.put(uuid, hash);

        // Send the waypoint data to the client.
        WaypointInfo.send(player, title, color, true);
    }
}
