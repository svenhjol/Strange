package svenhjol.strange.module.dimensions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public interface IDimensionClient {
    ResourceLocation getId();

    void register();

    default void handleWorldTick(ClientLevel level) {
        // override me
    }
}
