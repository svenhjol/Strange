package svenhjol.strange.module.knowledge2;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.KnowledgeMessages;
import svenhjol.strange.module.knowledge2.branch.BiomeBranch;
import svenhjol.strange.module.knowledge2.branch.DimensionBranch;
import svenhjol.strange.module.knowledge2.branch.StructureBranch;

@ClientModule(module = Knowledge2.class)
public class Knowledge2Client extends CharmModule {
    public static @Nullable BiomeBranch biomes;
    public static @Nullable DimensionBranch dimensions;
    public static @Nullable StructureBranch structures;

    @Override
    public void runWhenEnabled() {
        ClientEntityEvents.ENTITY_LOAD.register(this::handlePlayerJoin);
        ClientPlayNetworking.registerGlobalReceiver(KnowledgeMessages.CLIENT_SYNC_SEED, this::handleSyncSeed);
        ClientPlayNetworking.registerGlobalReceiver(KnowledgeMessages.CLIENT_SYNC_BIOMES, this::handleSyncBiomes);
        ClientPlayNetworking.registerGlobalReceiver(KnowledgeMessages.CLIENT_SYNC_DIMENSIONS, this::handleSyncDimensions);
        ClientPlayNetworking.registerGlobalReceiver(KnowledgeMessages.CLIENT_SYNC_STRUCTURES, this::handleSyncStructures);
    }

    private void handleSyncSeed(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        long seed = buffer.readLong();
        client.execute(() -> {
            Knowledge2.SEED = seed;
            LogHelper.debug(getClass(), "Received seed " + seed + " from server.");
        });
    }

    private void handleSyncBiomes(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null || tag.isEmpty()) return;
        client.execute(() -> {
            biomes = BiomeBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + biomes.size() + " biomes from server.");
        });
    }

    private void handleSyncDimensions(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null || tag.isEmpty()) return;
        client.execute(() -> {
            dimensions = DimensionBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + dimensions.size() + " dimensions from server.");
        });
    }

    private void handleSyncStructures(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null || tag.isEmpty()) return;
        client.execute(() -> {
            structures = StructureBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + structures.size() + " structures from server.");
        });
    }

    private void handlePlayerJoin(Entity entity, ClientLevel level) {
        if (!(entity instanceof LocalPlayer)) return;

        // Ask the server for seed and data to be sent.
        NetworkHelper.sendEmptyPacketToServer(KnowledgeMessages.SERVER_SYNC_SEED);
        NetworkHelper.sendEmptyPacketToServer(KnowledgeMessages.SERVER_SYNC_BIOMES);
        NetworkHelper.sendEmptyPacketToServer(KnowledgeMessages.SERVER_SYNC_DIMENSIONS);
        NetworkHelper.sendEmptyPacketToServer(KnowledgeMessages.SERVER_SYNC_STRUCTURES);
    }
}
