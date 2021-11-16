package svenhjol.strange.module.quests;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;

@ClientModule(module = Quests.class)
public class QuestsClient extends CharmModule {
    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_SHOW_QUEST_TOAST, this::handleshowQuestToast);
    }

    private void handleshowQuestToast(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        QuestToastType type = buffer.readEnum(QuestToastType.class);
        int tier = buffer.readInt();
        Component title = buffer.readComponent();

        client.execute(() -> client.getToasts().addToast(new QuestToast(type, tier, title)));
    }
}
