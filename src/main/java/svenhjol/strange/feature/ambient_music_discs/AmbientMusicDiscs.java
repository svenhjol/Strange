package svenhjol.strange.feature.ambient_music_discs;

import net.minecraft.sounds.SoundEvent;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.annotation.Feature;
import svenhjol.charmony.base.CharmFeature;
import svenhjol.strange.Strange;

import java.util.*;
import java.util.function.Supplier;

@Feature(mod = Strange.MOD_ID, description = "Music discs for each track of the Minecraft soundtrack.")
public class AmbientMusicDiscs extends CharmFeature {
    public static Map<String, Supplier<AmbientRecordItem>> items = new LinkedHashMap<>();
    public static Map<String, Supplier<SoundEvent>> sounds = new LinkedHashMap<>();
    public static final List<String> TRACKS = new LinkedList<>(List.of(
        "a_familiar_room",
        "aerie",
        "an_ordinary_day",
        "ancestry",
        "aria_math",
        "axolotl",
        "ballad_of_the_cats",
        "beginning_2",
        "biome_fest",
        "blind_spots",
        "bromeliad",
        "chrysopoeia",
        "clark",
        "comforting_memories",
        "concrete_halls",
        "credits",
        "crescent_dunes",
        "danny",
        "dead_voxel",
        "dragon_fish",
        "dreiton",
        "dry_hands",
        "echo_in_the_wind",
        "end",
        "end_boss",
        "firebugs",
        "floating_dream",
        "floating_trees",
        "haggstrom",
        "haunt_muskie",
        "infinite_amethyst",
        "key",
        "labyrinthine",
        "left_to_bloom",
        "living_mice",
        "mice_on_venus",
        "minecraft",
        "moog_city_2",
        "mutation",
        "one_more_day",
        "oxygene",
        "rubedo",
        "shuniji",
        "so_below",
        "stand_tall",
        "subwoofer_lullaby",
        "sweden",
        "taswell",
        "warmth",
        "wending",
        "wet_hands"
    ));

    @Configurable(name = "Attenuation support", description = "If true, ambient music discs will get quieter as the player moves away from the jukebox. EXPERIMENTAL!")
    public static boolean doExperimentalAttenuation = true;

    @Override
    public void register() {
        var registry = Strange.instance().registry();

        TRACKS.forEach(track -> sounds.put(track, registry.soundEvent("music_disc." + track)));
        TRACKS.forEach(track -> items.put(track, registry.item("music_disc_" + track,
            () -> new AmbientRecordItem(sounds.get(track).get()))));
    }
}
