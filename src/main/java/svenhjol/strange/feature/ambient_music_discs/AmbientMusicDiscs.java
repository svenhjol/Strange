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
        "chrysopoeia",
        "creative1",
        "creative2",
        "creative3",
        "creative4",
        "creative5",
        "creative6",
        "menu1",
        "menu2",
        "menu3",
        "menu4",
        "nether1",
        "nether2",
        "nether3",
        "nether4",
        "rubedo",
        "so_below"
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
