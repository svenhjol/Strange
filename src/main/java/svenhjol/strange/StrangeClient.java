package svenhjol.strange;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charm.base.handler.ClientHandler;

public class StrangeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientHandler.INSTANCE.registerFabricMod(Strange.MOD_ID);
    }
}
