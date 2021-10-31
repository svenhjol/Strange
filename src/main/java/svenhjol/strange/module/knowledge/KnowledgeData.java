package svenhjol.strange.module.knowledge;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.strange.module.knowledge.branches.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class KnowledgeData extends SavedData {
    private static final String TAG_SEED = "Seed";
    private static final List<KnowledgeBranch<?, ?>> branchInstances = new ArrayList<>();
    private static final Map<Character, KnowledgeBranch<?, ?>> mappedByStartRune = new HashMap<>();

    public SpecialsBranch specials = new SpecialsBranch();
    public DestinationsBranch destinations = new DestinationsBranch();
    public PlayersBranch players = new PlayersBranch();
    public StructuresBranch structures = new StructuresBranch();
    public DimensionsBranch dimensions = new DimensionsBranch();
    public BiomesBranch biomes = new BiomesBranch();
    public LocationsBranch locations = new LocationsBranch();

    @SuppressWarnings("unused")
    public KnowledgeData(@Nullable ServerLevel level) {
        this.setDirty();
    }

    public static KnowledgeData fromNbt(CompoundTag tag) {
        return fromNbt(null, tag);
    }

    public static KnowledgeData fromNbt(@Nullable ServerLevel level, CompoundTag tag) {
        KnowledgeData data = new KnowledgeData(level);

        data.specials = SpecialsBranch.load(tag);
        data.destinations = DestinationsBranch.load(tag);
        data.players = PlayersBranch.load(tag);
        data.biomes = BiomesBranch.load(tag);
        data.dimensions = DimensionsBranch.load(tag);
        data.structures = StructuresBranch.load(tag);
        data.locations = new LocationsBranch(); // not saved to disk

        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putLong(TAG_SEED, Knowledge.seed);

        specials.save(tag);
        biomes.save(tag);
        destinations.save(tag);
        dimensions.save(tag);
        players.save(tag);
        structures.save(tag);

        return tag;
    }

    public void populate(MinecraftServer server) {
        BuiltinRegistries.BIOME.entrySet().forEach(entry -> biomes.register(entry.getValue()));
        Registry.STRUCTURE_FEATURE.forEach(structure -> structures.register(structure));
        server.getAllLevels().forEach(level -> dimensions.register(level));
        server.getPlayerList().getPlayers().forEach(player -> players.register(player));
        this.setDirty();
    }

    public static List<KnowledgeBranch<?, ?>> getBranchInstances() {
        return branchInstances;
    }

    public static Map<Character, KnowledgeBranch<?, ?>> getMappedByStartRune() {
        return mappedByStartRune;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange" + dimensionType.getFileSuffix();
    }

    public static void clearAllData() {
        KnowledgeBranch.getBranches().forEach(KnowledgeBranch::clear);
    }
}
