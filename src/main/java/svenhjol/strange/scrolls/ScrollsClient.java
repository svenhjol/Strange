package svenhjol.strange.scrolls;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.ArrayList;
import java.util.List;

import static svenhjol.strange.scrolls.Scrolls.*;

public class ScrollsClient extends CharmClientModule {
    public static List<Quest> CACHED_CURRENT_QUESTS = new ArrayList<>();

    public ScrollsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_OPEN_SCROLL, this::handleClientOpenScroll);
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_SHOW_QUEST_TOAST, this::handleClientShowQuestToast);
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_CACHE_CURRENT_QUESTS, this::handleClientCacheCurrentQuests);

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

    private void handleClientShowQuestToast(PacketContext context, PacketByteBuf data) {
        CompoundTag questTag = data.readCompoundTag();
        QuestToastType type = data.readEnumConstant(QuestToastType.class);
        String title = data.readString();

        context.getTaskQueue().execute(() -> {
            Quest quest = Quest.getFromTag(questTag);
            MinecraftClient.getInstance().getToastManager().add(new QuestToast(quest, type, quest.getTitle(), title));
        });
    }

    private void handleClientCacheCurrentQuests(PacketContext context, PacketByteBuf data) {
        CompoundTag inTag = data.readCompoundTag();
        if (inTag == null || !inTag.contains("quests"))
            return;

        ListTag listTag = (ListTag)inTag.get("quests");
        if (listTag == null)
            return;

        CACHED_CURRENT_QUESTS.clear();

        for (Tag tag : listTag) {
            CACHED_CURRENT_QUESTS.add(Quest.getFromTag((CompoundTag)tag));
        }
    }
}
