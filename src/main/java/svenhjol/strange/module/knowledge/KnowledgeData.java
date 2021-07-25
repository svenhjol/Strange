package svenhjol.strange.module.knowledge;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class KnowledgeData extends SavedData {
    private static final int MAX_LEN = 23;
    private static final String TAG_SEED = "Seed";
    private static final String TAG_CALCULATED_POSITIONS = "CalculatedPositions";
    private static final String TAG_CALCULATED_STRUCTURES = "CalculatedStructures";
    private static final String TAG_CALCULATED_DIMENSIONS = "CalculatedDimensions";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_PLAYERS = "Players";
    private long seed;
    private Map<String, Table<BlockPos, ResourceLocation, ResourceLocation>> calculatedStructures = new HashMap<>();
    private Map<ResourceLocation, String> structures = new HashMap<>();
    private Map<ResourceLocation, String> dimensions = new HashMap<>();
    private Map<ResourceLocation, String> biomes = new HashMap<>();
    private Map<UUID, String> players = new HashMap<>();

    @SuppressWarnings("unused")
    public KnowledgeData(ServerLevel level) {
        this.setDirty();
    }

    public static KnowledgeData fromNbt(ServerLevel level, CompoundTag nbt) {
        KnowledgeData data = new KnowledgeData(level);
        clearAllData(data);

        data.seed = nbt.getLong(TAG_SEED);
        CompoundTag calculatedPositionsNbt = nbt.getCompound(TAG_CALCULATED_POSITIONS);
        CompoundTag calculatedStructuresNbt = nbt.getCompound(TAG_CALCULATED_STRUCTURES);
        CompoundTag calculatedDimensionsNbt = nbt.getCompound(TAG_CALCULATED_DIMENSIONS);
        CompoundTag playersNbt = nbt.getCompound(TAG_PLAYERS);
        CompoundTag biomesNbt = nbt.getCompound(TAG_BIOMES);
        CompoundTag structuresNbt = nbt.getCompound(TAG_STRUCTURES);
        CompoundTag dimensionsNbt = nbt.getCompound(TAG_DIMENSIONS);

        calculatedPositionsNbt.getAllKeys().forEach(key -> {
            long rawPos = calculatedPositionsNbt.getLong(key);
            String rawDimension = calculatedDimensionsNbt.getString(key);
            String rawStructure = calculatedStructuresNbt.getString(key);
            HashBasedTable<BlockPos, ResourceLocation, ResourceLocation> table = HashBasedTable.create();
            table.put(BlockPos.of(rawPos), new ResourceLocation(rawDimension), new ResourceLocation(rawStructure));
            data.calculatedStructures.put(key, table);
        });

        playersNbt.getAllKeys().forEach(key -> {
            String value = playersNbt.getString(key);
            data.players.put(UUID.fromString(key), value);
        });

        biomesNbt.getAllKeys().forEach(key -> {
            String value = biomesNbt.getString(key);
            data.biomes.put(new ResourceLocation(key), value);
        });

        structuresNbt.getAllKeys().forEach(key -> {
            String value = structuresNbt.getString(key);
            data.structures.put(new ResourceLocation(key), value);
        });

        dimensionsNbt.getAllKeys().forEach(key -> {
            String value = dimensionsNbt.getString(key);
            data.dimensions.put(new ResourceLocation(key), value);
        });

        return data;
    }

    public CompoundTag save(CompoundTag nbt) {
        CompoundTag calculatedPositionsNbt = new CompoundTag();
        CompoundTag calculatedStructuresNbt = new CompoundTag();
        CompoundTag calculatedDimensionsNbt = new CompoundTag();
        CompoundTag structuresNbt = new CompoundTag();
        CompoundTag dimensionsNbt = new CompoundTag();
        CompoundTag biomesNbt = new CompoundTag();
        CompoundTag playersNbt = new CompoundTag();

        this.calculatedStructures.forEach((key, table) ->
            table.rowMap().forEach((pos, row) -> {
                calculatedPositionsNbt.putLong(key, pos.asLong());
                row.forEach((dimension, structure) -> { calculatedDimensionsNbt.putString(key, dimension.toString());
                calculatedStructuresNbt.putString(key, structure.toString());
            });
        }));

        this.structures.forEach((res, key) -> structuresNbt.putString(res.toString(), key));
        this.dimensions.forEach((res, key) -> dimensionsNbt.putString(res.toString(), key));
        this.players.forEach((uuid, key) -> playersNbt.putString(uuid.toString(), key));
        this.biomes.forEach((res, key) -> biomesNbt.putString(res.toString(), key));

        nbt.put(TAG_CALCULATED_POSITIONS, calculatedPositionsNbt);
        nbt.put(TAG_CALCULATED_DIMENSIONS, calculatedDimensionsNbt);
        nbt.put(TAG_CALCULATED_STRUCTURES, calculatedStructuresNbt);
        nbt.put(TAG_STRUCTURES, structuresNbt);
        nbt.put(TAG_DIMENSIONS, dimensionsNbt);
        nbt.put(TAG_BIOMES, biomesNbt);
        nbt.put(TAG_PLAYERS, playersNbt);
        nbt.putLong(TAG_SEED, Knowledge.seed);

        return nbt;
    }

    public void populate(MinecraftServer server) {
        // tome data must match the configured seed or we have to regenerate all the knowledge entries
        if (Knowledge.seed != this.seed)
            clearAllData(this);

        BuiltinRegistries.BIOME.entrySet().forEach((e) -> this.registerBiome(e.getValue()));
        Registry.STRUCTURE_FEATURE.forEach(this::registerStructure);
        server.getAllLevels().forEach(this::registerLevel);
        server.getPlayerList().getPlayers().forEach(this::registerPlayer);
        this.setDirty();
    }

    public void registerStructure(StructureFeature<?> structure) {
        ResourceLocation res = Registry.STRUCTURE_FEATURE.getKey(structure);
        if (res != null && !this.structures.containsKey(res)) {
            this.structures.put(res, KnowledgeHelper.generateFromResource(res, MAX_LEN));
        }
    }

    public void registerBiome(Biome biome) {
        ResourceLocation res = BuiltinRegistries.BIOME.getKey(biome);
        if (res != null && !this.biomes.containsKey(res)) {
            this.biomes.put(res, KnowledgeHelper.generateFromResource(res, MAX_LEN));
        }
    }

    public void registerLevel(Level level) {
        ResourceLocation res = level.dimension().location();
        if (res != null && !this.dimensions.containsKey(res)) {
            this.dimensions.put(res, KnowledgeHelper.generateFromResource(res, MAX_LEN));
        }
    }

    public void registerPlayer(Player player) {
        UUID uuid = player.getUUID();
        if (uuid != null && !this.players.containsKey(uuid)) {
            this.players.put(uuid, KnowledgeHelper.generateFromString(uuid.toString(), MAX_LEN));
        }
    }

    public Map<ResourceLocation, String> getBiomes() {
        return biomes;
    }

    public Map<String, Table<BlockPos, ResourceLocation, ResourceLocation>> getCalculatedStructures() {
        return calculatedStructures;
    }

    public Map<ResourceLocation, String> getDimensions() {
        return dimensions;
    }

    public Map<UUID, String> getPlayers() {
        return players;
    }

    public Map<ResourceLocation, String> getStructures() {
        return structures;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_knowledge" + dimensionType.getFileSuffix();
    }

    public static void clearAllData(KnowledgeData data) {
        data.calculatedStructures = new HashMap<>();
        data.structures = new HashMap<>();
        data.dimensions = new HashMap<>();
        data.biomes = new HashMap<>();
        data.players = new HashMap<>();
    }
}
