package svenhjol.strange.module.journals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.module.journals.data.JournalInscription;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.journals.data.JournalNote;

import java.util.*;
import java.util.stream.Collectors;

public class JournalData {
    public static final int MAX_LOCATIONS = 200;

    private static final String TAG_RUNES = "Runes";
    private static final String TAG_INSCRIPTIONS = "Inscriptions";
    private static final String TAG_LOCATIONS = "Locations";
    private static final String TAG_STRUCTURES = "Structures";
    private static final String TAG_BIOMES = "Biomes";
    private static final String TAG_PLAYERS = "Players";
    private static final String TAG_NOTES = "Notes";

    private final UUID uuid;
    private List<Integer> runes = new ArrayList<>();

    private final List<JournalLocation> locations = new ArrayList<>();
    private final List<JournalInscription> inscriptions = new ArrayList<>();
    private final List<JournalNote> notes = new ArrayList<>();
    private final List<ResourceLocation> structures = new ArrayList<>();
    private final List<ResourceLocation> biomes = new ArrayList<>();
    private final List<UUID> players = new ArrayList<>();

    private JournalData(Player player) {
        this.uuid = player.getUUID();
    }

    public static JournalData fromNbt(Player player, CompoundTag nbt) {
        JournalData data = new JournalData(player);

        ListTag locationsNbt = nbt.getList(TAG_LOCATIONS, 10);
        ListTag inscriptionsNbt = nbt.getList(TAG_INSCRIPTIONS, 10);
        ListTag notesNbt = nbt.getList(TAG_NOTES, 10);
        ListTag structuresNbt = nbt.getList(TAG_STRUCTURES, 8);
        ListTag biomesNbt = nbt.getList(TAG_BIOMES, 8);
        ListTag playersNbt = nbt.getList(TAG_PLAYERS, 8);

        data.runes = Arrays.stream(nbt.getIntArray(TAG_RUNES)).boxed().collect(Collectors.toList());

        inscriptionsNbt.forEach(compound -> data.inscriptions.add(JournalInscription.fromNbt((CompoundTag)compound)));
        locationsNbt.forEach(compound -> data.locations.add(JournalLocation.fromNbt((CompoundTag)compound)));
        notesNbt.forEach(compound -> data.notes.add(JournalNote.fromNbt((CompoundTag)compound)));

        structuresNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.structures.add(new ResourceLocation(key)));

        biomesNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.biomes.add(new ResourceLocation(key)));

        playersNbt.stream().map(Tag::getAsString).map(i -> i.replace("\"", "")).forEach(
            key -> data.players.add(UUID.fromString(key)));

        return data;
    }

    public CompoundTag toNbt() {
        return toNbt(new CompoundTag());
    }

    public CompoundTag toNbt(CompoundTag nbt) {
        ListTag locationsNbt = new ListTag();
        ListTag inscriptionsNbt = new ListTag();
        ListTag notesNbt = new ListTag();
        ListTag structuresNbt = new ListTag();
        ListTag biomesNbt = new ListTag();
        ListTag playersNbt = new ListTag();

        locations.forEach(location
            -> locationsNbt.add(location.toNbt(new CompoundTag())));

        inscriptions.forEach(inscription
            -> inscriptionsNbt.add(inscription.toNbt(new CompoundTag())));

        notes.forEach(note
            -> notesNbt.add(note.toNbt(new CompoundTag())));

        structures.forEach(structure
            -> structuresNbt.add(StringTag.valueOf(structure.toString())));

        biomes.forEach(biome
            -> biomesNbt.add(StringTag.valueOf(biome.toString())));

        players.forEach(player
            -> playersNbt.add(StringTag.valueOf(player.toString())));

        nbt.putIntArray(TAG_RUNES, runes);
        nbt.put(TAG_LOCATIONS, locationsNbt);
        nbt.put(TAG_INSCRIPTIONS, inscriptionsNbt);
        nbt.put(TAG_NOTES, notesNbt);
        nbt.put(TAG_STRUCTURES, structuresNbt);
        nbt.put(TAG_BIOMES, biomesNbt);
        nbt.put(TAG_PLAYERS, playersNbt);

        return nbt;
    }

    public JournalLocation addLocation(Level level, BlockPos pos) {
        JournalLocation location = new JournalLocation(pos, DimensionHelper.getDimension(level));
        addLocation(location);
        return location;
    }

    public void updateLocation(JournalLocation location) {
        Optional<JournalLocation> opt = locations.stream().filter(l -> l.getId().equals(location.getId())).findFirst();
        opt.ifPresent(l -> l.populate(location));
    }

    public void addDeathLocation(Level level, BlockPos pos) {
        JournalLocation location = new JournalLocation(
            new TranslatableComponent("gui.strange.journal.death_location").getString(),
            pos,
            DimensionHelper.getDimension(level),
            JournalLocation.DEFAULT_DEATH_ICON,
            null
        );
        addLocation(location);
    }

    public List<Integer> getLearnedRunes() {
        return runes;
    }

    public List<JournalLocation> getLocations() {
        return locations;
    }

    public void learnRune(int val) {
        if (!runes.contains(val))
            runes.add(val);
    }

    public void learnBiome(ResourceLocation biome) {
        if (!biomes.contains(biome))
            biomes.add(biome);
    }

    public void learnStructure(ResourceLocation structure) {
        if (!structures.contains(structure))
            structures.add(structure);
    }

    private void addLocation(JournalLocation location) {
        if (locations.size() < MAX_LOCATIONS)
            locations.add(0, location);
    }
}
