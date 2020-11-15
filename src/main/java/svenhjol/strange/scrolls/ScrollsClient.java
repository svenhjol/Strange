package svenhjol.strange.scrolls;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.scrolls.tag.Quest;

import static svenhjol.strange.scrolls.Scrolls.MSG_CLIENT_OPEN_SCROLL;

public class ScrollsClient extends CharmClientModule {
    public ScrollsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        // listen for scroll open events being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_OPEN_SCROLL, (context, data) -> {
            CompoundTag questTag = data.readCompoundTag();
            context.getTaskQueue().execute(() -> {
                tryDisplayScroll(context.getPlayer(), questTag);
            });
        });
    }

    private void tryDisplayScroll(PlayerEntity player, CompoundTag questTag) {
        Quest quest = Quest.getFromTag(questTag);
        quest.update(player);
        MinecraftClient.getInstance().openScreen(new ScrollScreen(quest));
    }
}
