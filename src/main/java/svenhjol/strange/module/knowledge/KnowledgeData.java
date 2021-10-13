package svenhjol.strange.module.knowledge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.helper.LogHelper;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
public class KnowledgeData extends SavedData {
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 20;
    public static final String PREFIX_POSITIVE_BLOCKPOS = "a";
    public static final String PREFIX_NEGATIVE_BLOCKPOS = "b";
    public static final String PREFIX_LOCATION = "a";
    public static final String PREFIX_BIOME = "b";
    public static final String PREFIX_STRUCTURE = "c";
    public static final String PREFIX_DESTINATION = "e";
    public static final String PREFIX_PLAYER = "y";
    public static final String PREFIX_DIMENSION = "z";

    private static final String TAG_SEED = "Seed";
    private static final String TAG_DESTINATIONS = "Destinations";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_PLAYERS = "Players";

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

        return nbt;
    }

    public void populate(MinecraftServer server) {
        BuiltinRegistries.BIOME.entrySet().forEach((e) -> this.registerBiome(e.getValue()));
        Registry.STRUCTURE_FEATURE.forEach(this::registerStructure);
        server.getAllLevels().forEach(this::registerDimension);
        server.getPlayerList().getPlayers().forEach(this::registerPlayer);
        this.setDirty();
    }

    public void registerStructure(StructureFeature<?> structure) {
        ResourceLocation res = Registry.STRUCTURE_FEATURE.getKey(structure);
        if (res != null && !this.structures.containsKey(res)) {
            this.structures.put(res, PREFIX_STRUCTURE + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerBiome(Biome biome) {
        ResourceLocation res = BuiltinRegistries.BIOME.getKey(biome);
        if (res != null && !this.biomes.containsKey(res)) {
            this.biomes.put(res, PREFIX_BIOME + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerDimension(Level level) {
        ResourceLocation res = level.dimension().location();
        if (res != null && !this.dimensions.containsKey(res)) {
            this.dimensions.put(res, PREFIX_DIMENSION + KnowledgeHelper.generateStringFromResource(res, MAX_LENGTH));
        }
    }

    public void registerPlayer(Player player) {
        UUID uuid = player.getUUID();
        if (uuid != null && !this.players.containsKey(uuid)) {
            this.players.put(uuid, PREFIX_PLAYER + KnowledgeHelper.generateStringFromString(uuid.toString(), MAX_LENGTH));
        }
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

    public Optional<Destination> getDestination(String runes) {
        return Optional.ofNullable(destinations.get(runes));
    }

    /**
     * A destination is used by runestone generation to specify a location to send the player to.
     *
     * @param dimension Dimension where the destination can be found.
     * @param location Resource ID of the destination's location (e.g. "minecraft:mansion", "minecraft:ice_spikes").
     * @param pos BlockPos of the destination. Null means it hasn't been resolved yet.
     * @return Rune string that represents this new destination.
     */
    public Optional<Destination> createDestination(Random random, float difficulty, ResourceLocation dimension, ResourceLocation location, @Nullable BlockPos pos) {
        int tries = 0;
        int maxTries = 10;
        boolean foundUniqueRunes = false;
        String runes = "";

        // keep trying to find a unique rune string for this destination
        while (!foundUniqueRunes && tries++ < maxTries) {
            runes = KnowledgeHelper.generateDestinationString(random, difficulty);
            foundUniqueRunes = !this.destinations.containsKey(runes);
        }

        if (!foundUniqueRunes) {
            LogHelper.debug(this.getClass(), "Could not find unique rune string for this destination, giving up");
            return Optional.empty();
        }

        String prefixed = PREFIX_DESTINATION + runes;
        if (pos == null) {
            pos = BlockPos.ZERO;
        }

        Destination dest = new Destination();
        dest.runes = prefixed;
        dest.pos = pos;
        dest.dimension = dimension;
        dest.location = location;
        dest.items = Arrays.asList(new ItemStack(Items.DIAMOND)); // TODO: testdata

        this.destinations.put(prefixed, dest);
        this.setDirty();

        return Optional.of(dest);
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
    }
}
