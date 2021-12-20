package svenhjol.strange.module.knowledge2;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.KnowledgeMessages;
import svenhjol.strange.module.runes.Runes;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Knowledge2 extends CharmModule {
    private static Knowledge2Data knowledgeData;

    // This is set to the seed of the loaded overworld level.
    public static long SEED = Long.MIN_VALUE;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_SEED, this::handleSyncSeed);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_BIOMES, this::handleSyncBiomes);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_DIMENSIONS, this::handleSyncDimensions);
        ServerPlayNetworking.registerGlobalReceiver(KnowledgeMessages.SERVER_SYNC_STRUCTURES, this::handleSyncStructures);
    }

    public static Optional<Knowledge2Data> getKnowledge() {
        return Optional.ofNullable(knowledgeData);
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
                tag -> Knowledge2Data.load(overworld, tag),
                () -> new Knowledge2Data(overworld),
                Knowledge2Data.getFileId(level.dimensionType())
            );

            Runes.addBranch(knowledgeData.biomeBranch);
            Runes.addBranch(knowledgeData.dimensionBranch);
            Runes.addBranch(knowledgeData.structureBranch);

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
        NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_SEED, buf -> buf.writeLong(SEED));
    }

    private void handleSyncBiomes(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.biomeBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_BIOMES, buf -> buf.writeNbt(tag));
        });
    }

    private void handleSyncDimensions(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.dimensionBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_DIMENSIONS, buf -> buf.writeNbt(tag));
        });
    }

    private void handleSyncStructures(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        getKnowledge().ifPresent(knowledge -> {
            CompoundTag tag = new CompoundTag();
            knowledge.structureBranch.save(tag);
            NetworkHelper.sendPacketToClient(player, KnowledgeMessages.CLIENT_SYNC_STRUCTURES, buf -> buf.writeNbt(tag));
        });
    }
}
