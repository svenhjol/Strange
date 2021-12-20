package svenhjol.strange.module.discoveries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runes.Runes;

import javax.annotation.Nullable;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Discoveries extends CharmModule {
    private static @Nullable DiscoveryData discoveryData;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
    }

    public static Optional<DiscoveryData> getDiscoveries() {
        return Optional.ofNullable(discoveryData);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            var overworld = (ServerLevel) level;
            var storage = overworld.getDataStorage();

            discoveryData = storage.computeIfAbsent(
                tag -> DiscoveryData.load(overworld, tag),
                () -> new DiscoveryData(overworld),
                DiscoveryData.getFileId(level.dimensionType())
            );

            Runes.addBranch(discoveryData.branch);
        }
    }
}
