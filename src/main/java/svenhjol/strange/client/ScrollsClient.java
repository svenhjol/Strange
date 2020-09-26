package svenhjol.strange.client;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import svenhjol.meson.MesonModule;
import svenhjol.strange.scroll.ScrollScreen;
import svenhjol.strange.scroll.tag.QuestTag;

import static svenhjol.strange.module.Scrolls.MSG_CLIENT_OPEN_SCROLL;

public class ScrollsClient {
    private final MesonModule module;

    public ScrollsClient(MesonModule module) {
        this.module = module;

        // listen for scroll open events being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_OPEN_SCROLL, (context, data) -> {
            CompoundTag questTag = data.readCompoundTag();
            context.getTaskQueue().execute(() -> {
                tryDisplayScroll(context.getPlayer(), questTag);
            });
        });
    }

    private void tryDisplayScroll(PlayerEntity player, CompoundTag questTag) {
        QuestTag quest = new QuestTag();
        quest.fromTag(questTag);
        quest.update(player);
        MinecraftClient.getInstance().openScreen(new ScrollScreen(quest));
    }
}
