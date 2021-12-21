package svenhjol.strange.module.knowledge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.command.KnowledgeCommand;
import svenhjol.strange.module.knowledge.network.ServerSendBiomes;
import svenhjol.strange.module.knowledge.network.ServerSendDimensions;
import svenhjol.strange.module.knowledge.network.ServerSendSeed;
import svenhjol.strange.module.knowledge.network.ServerSendStructures;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Knowledge extends CharmModule {
    public static ServerSendSeed SEND_SEED;
    public static ServerSendBiomes SEND_BIOMES;
    public static ServerSendDimensions SEND_DIMENSIONS;
    public static ServerSendStructures SEND_STRUCTURES;

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

        SEND_SEED = new ServerSendSeed();
        SEND_BIOMES = new ServerSendBiomes();
        SEND_DIMENSIONS = new ServerSendDimensions();
        SEND_STRUCTURES = new ServerSendStructures();
    }

    public static Optional<KnowledgeData> getKnowledge() {
        return Optional.ofNullable(knowledgeData);
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();

        SEND_SEED.send(player);
        SEND_BIOMES.send(player);
        SEND_DIMENSIONS.send(player);
        SEND_STRUCTURES.send(player);
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
}
