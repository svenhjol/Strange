package svenhjol.strange.scrolls;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.scrolls.tag.Quest;

import static svenhjol.strange.scrolls.Scrolls.MSG_CLIENT_OPEN_SCROLL;
import static svenhjol.strange.scrolls.Scrolls.MSG_CLIENT_QUEST_TOAST;

public class ScrollsClient extends CharmClientModule {
    public ScrollsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        // listen for scroll open events being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_OPEN_SCROLL, this::handleClientOpenScroll);

        // listen for quest toast update events being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_QUEST_TOAST, this::handleClientQuestToast);

        // set up scroll item model predicate
        ModelPredicateProviderRegistryAccessor.callRegister(new Identifier("scroll_state"), (stack, world, entity)
            -> ScrollItem.hasBeenOpened(stack) ? 0.1F : 0.0F);
    }

    private void handleClientOpenScroll(PacketContext context, PacketByteBuf data) {
        CompoundTag questTag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            Quest quest = Quest.getFromTag(questTag);
            quest.update(context.getPlayer());
            MinecraftClient.getInstance().openScreen(new ScrollScreen(quest));
        });
    }

    private void handleClientQuestToast(PacketContext context, PacketByteBuf data) {
        CompoundTag questTag = data.readCompoundTag();
        QuestToastType type = data.readEnumConstant(QuestToastType.class);
        String title = data.readString();
        context.getTaskQueue().execute(() -> {
            Quest quest = Quest.getFromTag(questTag);
            MinecraftClient.getInstance().getToastManager().add(new QuestToast(quest, type, quest.getTitle(), title));
        });
    }
}
