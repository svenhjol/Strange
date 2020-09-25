package svenhjol.strange.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import svenhjol.meson.MesonModule;
import svenhjol.strange.gui.ScrollScreen;
import svenhjol.strange.scroll.ScrollQuest;

import static svenhjol.strange.module.Scrolls.MSG_CLIENT_OPEN_SCROLL;

@Environment(EnvType.CLIENT)
public class ScrollsClient {
    private final MesonModule module;

    public ScrollsClient(MesonModule module) {
        this.module = module;

        // listen for scroll open events being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_OPEN_SCROLL, (context, data) -> {
            CompoundTag questTag = data.readCompoundTag();
            context.getTaskQueue().execute(() -> {
                tryDisplayScroll(questTag);
            });
        });
    }

    private void tryDisplayScroll(CompoundTag questTag) {
        ScrollQuest quest = new ScrollQuest();
        quest.fromTag(questTag);
        MinecraftClient.getInstance().openScreen(new ScrollScreen(quest));
    }
}
