package svenhjol.strange.module.knowledge;

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
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class KnowledgeData extends SavedData {
    public static final int MIN_LENGTH = 6;
    public static final int MAX_LENGTH = 24;
    public static final String NOVICE_RUNES = "abcdef";
    public static final String APPRENTICE_RUNES = "ghijkl";
    public static final String JOURNEYMAN_RUNES = "mnopqr";
    public static final String EXPERT_RUNES = "stuv";
    public static final String MASTER_RUNES = "wxyz";

    public char SPAWN_RUNE = 0;
    public char BIOME_RUNE = 0;
    public char STRUCTURE_RUNE = 0;
    public char LOCATION_RUNE = 0;
    public char DESTINATION_RUNE = 0;
    public char PLAYER_RUNE = 0;
    public char DIMENSION_RUNE = 0;
    public char POSITIVE_RUNE = 0;
    public char NEGATIVE_RUNE = 0;

    private static final String TAG_SEED = "Seed";
    private static final String TAG_DESTINATIONS = "Destinations";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_PLAYERS = "Players";
    private static final String TAG_PREFIXES = "Prefixes";

    private static final String TAG_SPAWN_RUNE = "SpawnRune";
    private static final String TAG_BIOME_RUNE = "BiomeRune";
    private static final String TAG_STRUCTURE_RUNE = "StructureRune";
    private static final String TAG_LOCATION_RUNE = "LocationRune";
    private static final String TAG_DESTINATION_RUNE = "DestinationRune";
    private static final String TAG_PLAYER_RUNE = "PlayerRune";
    private static final String TAG_DIMENSION_RUNE = "DimensionRune";
    private static final String TAG_POSITIVE_RUNE = "PositiveRune";
    private static final String TAG_NEGATIVE_RUNE = "NegativeRune";

    private Map<String, Destination> destinations = new HashMap<>();
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

        CompoundTag destinationsNbt = nbt.getCompound(TAG_DESTINATIONS);
        CompoundTag playersNbt = nbt.getCompound(TAG_PLAYERS);
        CompoundTag biomesNbt = nbt.getCompound(TAG_BIOMES);
        CompoundTag structuresNbt = nbt.getCompound(TAG_STRUCTURES);
        CompoundTag dimensionsNbt = nbt.getCompound(TAG_DIMENSIONS);
        CompoundTag prefixesNbt = nbt.getCompound(TAG_PREFIXES);

        data.SPAWN_RUNE = (char)prefixesNbt.getInt(TAG_SPAWN_RUNE);
        data.BIOME_RUNE = (char)prefixesNbt.getInt(TAG_BIOME_RUNE);
        data.STRUCTURE_RUNE = (char)prefixesNbt.getInt(TAG_STRUCTURE_RUNE);
        data.LOCATION_RUNE = (char)prefixesNbt.getInt(TAG_LOCATION_RUNE);
        data.DESTINATION_RUNE = (char)prefixesNbt.getInt(TAG_DESTINATION_RUNE);
        data.PLAYER_RUNE = (char)prefixesNbt.getInt(TAG_PLAYER_RUNE);
        data.DIMENSION_RUNE = (char)prefixesNbt.getInt(TAG_DIMENSION_RUNE);
        data.POSITIVE_RUNE = (char)prefixesNbt.getInt(TAG_POSITIVE_RUNE);
        data.NEGATIVE_RUNE = (char)prefixesNbt.getInt(TAG_NEGATIVE_RUNE);

        destinationsNbt.getAllKeys().forEach(key -> {
            Destination destination = Destination.fromTag(destinationsNbt.getCompound(key));
            data.destinations.put(key, destination);
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
        CompoundTag destinationsNbt = new CompoundTag();
        CompoundTag structuresNbt = new CompoundTag();
        CompoundTag dimensionsNbt = new CompoundTag();
        CompoundTag biomesNbt = new CompoundTag();
        CompoundTag playersNbt = new CompoundTag();
        CompoundTag prefixesNbt = new CompoundTag();

        this.destinations.forEach((key, dest) -> destinationsNbt.put(key, dest.toTag()));
        this.structures.forEach((res, key) -> structuresNbt.putString(res.toString(), key));
        this.dimensions.forEach((res, key) -> dimensionsNbt.putString(res.toString(), key));
        this.players.forEach((uuid, key) -> playersNbt.putString(uuid.toString(), key));
        this.biomes.forEach((res, key) -> biomesNbt.putString(res.toString(), key));

        nbt.put(TAG_DESTINATIONS, destinationsNbt);
        nbt.put(TAG_STRUCTURES, structuresNbt);
        nbt.put(TAG_DIMENSIONS, dimensionsNbt);
        nbt.put(TAG_BIOMES, biomesNbt);
        nbt.put(TAG_PLAYERS, playersNbt);
        nbt.putLong(TAG_SEED, Knowledge.seed);

        prefixesNbt.putInt(TAG_SPAWN_RUNE, SPAWN_RUNE);
        prefixesNbt.putInt(TAG_BIOME_RUNE, BIOME_RUNE);
        prefixesNbt.putInt(TAG_STRUCTURE_RUNE, STRUCTURE_RUNE);
        prefixesNbt.putInt(TAG_LOCATION_RUNE, LOCATION_RUNE);
        prefixesNbt.putInt(TAG_DESTINATION_RUNE, DESTINATION_RUNE);
        prefixesNbt.putInt(TAG_PLAYER_RUNE, PLAYER_RUNE);
        prefixesNbt.putInt(TAG_DIMENSION_RUNE, DIMENSION_RUNE);
        prefixesNbt.putInt(TAG_POSITIVE_RUNE, POSITIVE_RUNE);
        prefixesNbt.putInt(TAG_NEGATIVE_RUNE, NEGATIVE_RUNE);
        nbt.put(TAG_PREFIXES, prefixesNbt);

        return nbt;
    }

    public void populate(MinecraftServer server) {
        this.registerPrefixes();
        BuiltinRegistries.BIOME.entrySet().forEach((e) -> this.registerBiome(e.getValue()));
        Registry.STRUCTURE_FEATURE.forEach(this::registerStructure);
        server.getAllLevels().forEach(this::registerDimension);
        server.getPlayerList().getPlayers().forEach(this::registerPlayer);
        this.setDirty();
    }

    public void registerBiome(Biome biome) {
        ResourceLocation res = BuiltinRegistries.BIOME.getKey(biome);
        if (res != null && !this.biomes.containsKey(res)) {
            this.biomes.put(res, BIOME_RUNE + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerStructure(StructureFeature<?> structure) {
        ResourceLocation res = Registry.STRUCTURE_FEATURE.getKey(structure);
        if (res != null && !this.structures.containsKey(res)) {
            this.structures.put(res, STRUCTURE_RUNE + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerDimension(Level level) {
        ResourceLocation res = level.dimension().location();
        if (res != null && !this.dimensions.containsKey(res)) {
            this.dimensions.put(res, DIMENSION_RUNE + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerPlayer(Player player) {
        UUID uuid = player.getUUID();
        if (uuid != null && !this.players.containsKey(uuid)) {
            this.players.put(uuid, PLAYER_RUNE + KnowledgeHelper.generateStringFromString(uuid.toString(), MAX_LENGTH));
        }
    }

    public void registerPrefixes() {
        SPAWN_RUNE = KnowledgeHelper.getCharFromRange(NOVICE_RUNES,0);
        LOCATION_RUNE = KnowledgeHelper.getCharFromRange(NOVICE_RUNES,1);
        DESTINATION_RUNE = KnowledgeHelper.getCharFromRange(NOVICE_RUNES, 2);

        BIOME_RUNE = KnowledgeHelper.getCharFromRange(APPRENTICE_RUNES,0);
        STRUCTURE_RUNE = KnowledgeHelper.getCharFromRange(APPRENTICE_RUNES,1);
        POSITIVE_RUNE = KnowledgeHelper.getCharFromRange(APPRENTICE_RUNES,2);
        NEGATIVE_RUNE = KnowledgeHelper.getCharFromRange(APPRENTICE_RUNES,3);

        PLAYER_RUNE = KnowledgeHelper.getCharFromRange(JOURNEYMAN_RUNES,0);
        DIMENSION_RUNE = KnowledgeHelper.getCharFromRange(JOURNEYMAN_RUNES,1);
    }

    public Map<ResourceLocation, String> getBiomes() {
        return biomes;
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

    public Map<String, Destination> getDestinations() {
        return this.destinations;
    }

    public boolean hasDestination(String runes) {
        return destinations.containsKey(runes);
    }

    public void updateDestination(String runes, Destination destination) {
        this.destinations.put(runes, destination);
        this.setDirty();
    }

    public Optional<Destination> getDestination(String runes) {
        return Optional.ofNullable(destinations.get(runes));
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_knowledge" + dimensionType.getFileSuffix();
    }

    public static void clearAllData(KnowledgeData data) {
        data.destinations = new HashMap<>();
        data.structures = new HashMap<>();
        data.dimensions = new HashMap<>();
        data.biomes = new HashMap<>();
        data.players = new HashMap<>();

        data.SPAWN_RUNE = 0;
        data.DIMENSION_RUNE = 0;
        data.PLAYER_RUNE = 0;
        data.LOCATION_RUNE = 0;
        data.STRUCTURE_RUNE = 0;
        data.BIOME_RUNE = 0;
        data.DESTINATION_RUNE = 0;
    }
}
