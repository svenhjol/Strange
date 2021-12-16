package svenhjol.strange.init;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public class StrangeEvents {
    public static final ResourceLocation WORLD_LOAD_PHASE = new ResourceLocation(Strange.MOD_ID, "world_load_phase");

    public static void init() {
        ServerWorldEvents.LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, StrangeEvents.WORLD_LOAD_PHASE);
    }
}
