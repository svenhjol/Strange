package svenhjol.strange.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.LinkedList;

public interface AddRunestoneDestinationCallback {
    Event<AddRunestoneDestinationCallback> EVENT = EventFactory.createArrayBacked(AddRunestoneDestinationCallback.class, (listeners) -> (level, destinations) -> {
        for (AddRunestoneDestinationCallback listener : listeners) {
            listener.interact(level, destinations);
        }
    });

    void interact(Level level, LinkedList<ResourceLocation> destinations);
}
