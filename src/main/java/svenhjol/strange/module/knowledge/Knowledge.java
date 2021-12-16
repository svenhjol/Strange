package svenhjol.strange.module.knowledge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class Knowledge extends CharmModule {
    public static final ResourceLocation MSG_SERVER_SYNC_KNOWLEDGE = new ResourceLocation(Strange.MOD_ID, "server_sync_knowledge");
    public static final ResourceLocation MSG_CLIENT_SYNC_KNOWLEDGE = new ResourceLocation(Strange.MOD_ID, "client_sync_knowledge");

    public static final int NUM_RUNES = 26;
    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 23;
    public static final int ALPHABET_START = 97;
    public static final int ALPHABET_END = 122;
    public static final int TIERS = 5;
    public static final Map<Tier, String> TIER_RUNE_SETS = new HashMap<>();

    public static long seed;
    public static KnowledgeData knowledge;

    @Override
    public void register() {
        TIER_RUNE_SETS.put(Tier.NOVICE, "abcdef");
        TIER_RUNE_SETS.put(Tier.APPRENTICE, "ghijkl");
        TIER_RUNE_SETS.put(Tier.JOURNEYMAN, "mnopqr");
        TIER_RUNE_SETS.put(Tier.EXPERT, "stuv");
        TIER_RUNE_SETS.put(Tier.MASTER, "wxyz");

        KnowledgeCommand.init();
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_SYNC_KNOWLEDGE, this::handleSyncKnowledge);
    }

    public static Optional<KnowledgeData> getKnowledgeData() {
        return Optional.ofNullable(knowledge);
    }

    private void handleSyncKnowledge(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = new CompoundTag();
        knowledge.save(tag);
        NetworkHelper.sendPacketToClient(player, MSG_CLIENT_SYNC_KNOWLEDGE, buf -> buf.writeNbt(tag));
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            // overworld gets loaded first, set up the knowledge data here
            ServerLevel overworld = (ServerLevel)level;

            seed = overworld.getSeed();
            DimensionDataStorage storage = overworld.getDataStorage();

            knowledge = storage.computeIfAbsent(
                nbt -> KnowledgeData.fromNbt(overworld, nbt),
                () -> new KnowledgeData(overworld),
                KnowledgeData.getFileId(overworld.getLevel().dimensionType()));

            knowledge.populate(server);
            LogHelper.info(this.getClass(), "Loaded knowledge saved data");

        } else {
            getKnowledgeData().ifPresent(data -> {
                // capture the world that just got loaded
                data.dimensions.register(level);
                data.setDirty();
            });
        }
    }

    public enum Tier implements ICharmEnum {
        TEST,
        NOVICE,
        APPRENTICE,
        JOURNEYMAN,
        EXPERT,
        MASTER;

        @Nullable
        public static Tier getByOrdinal(int o) {
            for (Tier value : values()) {
                if (value.ordinal() == o) {
                    return value;
                }
            }
            return null;
        }
    }
}
