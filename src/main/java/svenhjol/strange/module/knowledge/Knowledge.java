package svenhjol.strange.module.knowledge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.KnowledgeMessages;
import svenhjol.strange.module.knowledge.command.KnowledgeCommand;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Knowledge extends CharmModule {
    private static KnowledgeData knowledgeData;

    // This is set to the seed of the loaded overworld level.
    public static long SEED = Long.MIN_VALUE;

    @Override
    public void register() {
        KnowledgeCommand.init();
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_SEED, this::handleSyncSeed);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_BIOMES, this::handleSyncBiomes);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_DIMENSIONS, this::handleSyncDimensions);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_STRUCTURES, this::handleSyncStructures);
    }

    public static Optional<KnowledgeData> getKnowledge() {
        return Optional.ofNullable(knowledgeData);
    }

    public static void sendSeed(ServerPlayer player) {
        NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_SEED, buf -> buf.writeLong(SEED));
    }

    public static void sendBiomes(ServerPlayer player) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.biomeBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_BIOMES, buf -> buf.writeNbt(tag));
        });
    }

    public static void sendDimensions(ServerPlayer player) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.dimensionBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_DIMENSIONS, buf -> buf.writeNbt(tag));
        });
    }

    public static void sendStructures(ServerPlayer player) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.structureBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_STRUCTURES, buf -> buf.writeNbt(tag));
        });
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        sendSeed(player);
        sendBiomes(player);
        sendDimensions(player);
        sendStructures(player);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {

        // Overworld is loaded first so we can setup the knowledge storage just once
        // and then register other dimensions that get loaded after the overworld.
        if (level.dimension() == Level.OVERWORLD) {

            // Set the seed to that of the overworld.
            // This is used for the rune helper RNG.
            ServerLevel overworld = (ServerLevel) level;
            SEED = overworld.getSeed();

            // Set up knowledge and register world things.
            DimensionDataStorage storage = overworld.getDataStorage();
            knowledgeData = storage.computeIfAbsent(
                tag -> KnowledgeData.load(overworld, tag),
                () -> new KnowledgeData(overworld),
                KnowledgeData.getFileId(level.dimensionType())
            );

            Registry.STRUCTURE_FEATURE.forEach(structure -> knowledgeData.structureBranch.register(structure));
            BuiltinRegistries.BIOME.entrySet().forEach(entry -> knowledgeData.biomeBranch.register(entry.getValue()));
            knowledgeData.dimensionBranch.register(level);
            knowledgeData.setDirty();

        } else {

            // Register the dimension that just loaded.
            getKnowledge().ifPresent(knowledge -> {
                knowledge.dimensionBranch.register(level);
                knowledge.setDirty();
            });

        }
    }

    private void handleSyncSeed(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendSeed(player));
    }

    private void handleSyncBiomes(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendBiomes(player));
    }

    private void handleSyncDimensions(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendDimensions(player));
    }

    private void handleSyncStructures(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendStructures(player));
    }
}
