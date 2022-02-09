package svenhjol.strange.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.discoveries.Discovery;

public interface InteractDiscoveryCallback {
    Event<InteractDiscoveryCallback> EVENT = EventFactory.createArrayBacked(InteractDiscoveryCallback.class, listeners -> (player, discovery) -> {
        for (InteractDiscoveryCallback listener : listeners) {
            listener.interact(player, discovery);
        }
    });

    void interact(ServerPlayer player, Discovery discovery);
}
