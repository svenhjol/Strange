package svenhjol.strange.module.knowledge;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;

import javax.annotation.Nullable;
import java.util.Optional;

@ClientModule(module = Knowledge.class)
public class KnowledgeClient extends CharmModule {
    public static KnowledgeData knowledge;

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(Knowledge.MSG_CLIENT_SYNC_KNOWLEDGE, this::handleSyncKnowledge);
    }

    public static Optional<KnowledgeData> getKnowledgeData() {
        return Optional.ofNullable(knowledge);
    }

    public void handleSyncKnowledge(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        updateKnowledge(buffer.readNbt());
    }

    public static void sendSyncKnowledge() {
        NetworkHelper.sendEmptyPacketToServer(Knowledge.MSG_SERVER_SYNC_KNOWLEDGE);
    }

    private void updateKnowledge(@Nullable CompoundTag tag) {
        if (tag != null) {
            knowledge = KnowledgeData.fromNbt(tag);
        }
    }
}

