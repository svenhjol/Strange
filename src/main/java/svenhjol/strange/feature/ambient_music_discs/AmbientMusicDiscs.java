package svenhjol.strange.feature.ambient_music_discs;

import net.minecraft.sounds.SoundEvent;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.annotation.Feature;
import svenhjol.charmony.base.CharmFeature;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Feature(mod = Strange.MOD_ID, description = "Music discs for each track of the Minecraft soundtrack.")
public class AmbientMusicDiscs extends CharmFeature {
    public static Map<String, Supplier<AmbientRecordItem>> items = new HashMap<>();
    public static Map<String, Supplier<SoundEvent>> sounds = new HashMap<>();
    public static final List<String> TRACKS = List.of(
        "a_familiar_room",
        "aerie",
        "an_ordinary_day",
        "ancestry",
        "axolotl",
        "bromeliad",
        "calm1",
        "calm2",
        "calm3",
        "chrysopoeia",
        "comforting_memories",
        "creative1",
        "creative2",
        "creative3",
        "creative4",
        "creative5",
        "creative6",
        "credits",
        "crescent_dunes",
        "dragon",
        "dragon_fish",
        "echo_in_the_wind",
        "end",
        "firebugs",
        "floating_dream",
        "hal1",
        "hal2",
        "hal3",
        "hal4",
        "infinite_amethyst",
        "labyrinthine",
        "left_to_bloom",
        "menu1",
        "menu2",
        "menu3",
        "menu4",
        "nuance1",
        "nuance2",
        "nether1",
        "nether2",
        "nether3",
        "nether4",
        "one_more_day",
        "piano1",
        "piano2",
        "piano3",
        "rubedo",
        "shuniji",
        "so_below",
        "stand_tall",
        "wending"
    );

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
