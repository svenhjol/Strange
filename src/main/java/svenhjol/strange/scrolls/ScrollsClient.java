package svenhjol.strange.scrolls;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.scrolls.tag.Quest;
import svenhjol.strange.traveljournals.gui.ActiveScrollsScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        ClientSidePacketRegistry.INSTANCE.register(MSG_CLIENT_DESTROY_SCROLL, this::handleClientDestroyScroll);

        // set up scroll item model predicate
        ModelPredicateProviderRegistryAccessor.callRegister(new Identifier("scroll_state"), (stack, world, entity, i)
            -> ScrollItem.hasBeenOpened(stack) ? 0.1F : 0.0F);
    }

    private void handleClientOpenScroll(PacketContext context, PacketByteBuf data) {
        CompoundTag questTag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            Quest quest = Quest.getFromTag(questTag);
            quest.update(context.getPlayer());

            boolean backToJournal = mc.currentScreen instanceof ActiveScrollsScreen;
            mc.openScreen(new ScrollScreen(quest, backToJournal));
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

    private void handleClientDestroyScroll(PacketContext context, PacketByteBuf data) {
        context.getTaskQueue().execute(() -> {
            PlayerEntity player = context.getPlayer();
            if (player == null) return;

            double spread = 1.1D;
            Random random = player.world.random;
            for (int i = 0; i < 40; i++) {
                double px = player.getBlockPos().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.getBlockPos().getY() + 0.5D;
                double pz = player.getBlockPos().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.world.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
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
