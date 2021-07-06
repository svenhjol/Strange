package svenhjol.strange.module.scrolls;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.scrolls.nbt.Quest;
import svenhjol.strange.module.travel_journals.screen.TravelJournalScrollsScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static svenhjol.strange.module.scrolls.Scrolls.*;

@ClientModule(module = Scrolls.class)
public class ScrollsClient extends CharmModule {
    public static List<Quest> CACHED_CURRENT_QUESTS = new ArrayList<>();

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(MSG_CLIENT_OPEN_SCROLL, this::handleClientOpenScroll);
        ClientPlayNetworking.registerGlobalReceiver(MSG_CLIENT_SHOW_QUEST_TOAST, this::handleClientShowQuestToast);
        ClientPlayNetworking.registerGlobalReceiver(MSG_CLIENT_CACHE_CURRENT_QUESTS, this::handleClientCacheCurrentQuests);
        ClientPlayNetworking.registerGlobalReceiver(MSG_CLIENT_DESTROY_SCROLL, this::handleClientDestroyScroll);

        // set up scroll item model predicate
        ModelPredicateProviderRegistryAccessor.callRegister(new ResourceLocation("scroll_state"), (stack, world, entity, i)
            -> ScrollItem.hasBeenOpened(stack) ? 1.0F : 0.0F);
    }

    private void handleClientOpenScroll(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag questTag = data.readNbt();
        client.execute(() -> {
            Minecraft mc = Minecraft.getInstance();
            Quest quest = Quest.getFromNbt(questTag);
            quest.update(client.player);

            boolean backToJournal = mc.screen instanceof TravelJournalScrollsScreen;
            mc.setScreen(new ScrollScreen(quest, backToJournal));
        });
    }

    private void handleClientShowQuestToast(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag questTag = data.readNbt();
        QuestToastType type = data.readEnum(QuestToastType.class);
        String title = data.readUtf();

        client.execute(() -> {
            Quest quest = Quest.getFromNbt(questTag);
            Minecraft.getInstance().getToasts().addToast(new QuestToast(quest, type, quest.getTitle(), title));
        });
    }

    private void handleClientDestroyScroll(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        client.execute(() -> {
            Player player = client.player;
            if (player == null) return;

            double spread = 1.1D;
            Random random = player.level.random;
            for (int i = 0; i < 40; i++) {
                double px = player.blockPosition().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.blockPosition().getY() + 0.5D;
                double pz = player.blockPosition().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        });
    }

    private void handleClientCacheCurrentQuests(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag inTag = data.readNbt();
        if (inTag == null || !inTag.contains(QUESTS_NBT))
            return;

        ListTag listTag = (ListTag)inTag.get(QUESTS_NBT);
        if (listTag == null)
            return;

        CACHED_CURRENT_QUESTS.clear();

        for (Tag tag : listTag) {
            CACHED_CURRENT_QUESTS.add(Quest.getFromNbt((CompoundTag)tag));
        }
    }
}
