package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class StrangeSounds
{
    public static List<SoundEvent> soundsToRegister = new ArrayList<>();

    // music
    public static final SoundEvent MUSIC_THARNA = createSound("music.tharna");
    public static final SoundEvent MUSIC_STEINN = createSound("music.steinn");
    public static final SoundEvent MUSIC_MUS = createSound("music.mus");
    public static final SoundEvent MUSIC_UNDIR = createSound("music.undir");
    public static final SoundEvent MUSIC_DISC = createSound("music_disc.strange");

    // action/event sounds
    public static final SoundEvent QUEST_ACTION_COMPLETE = createSound("quest_action_complete");
    public static final SoundEvent QUEST_ACTION_COUNT = createSound("quest_action_count");
    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");
    public static final SoundEvent SCREENSHOT = createSound("screenshot");
    public static final SoundEvent SPELL_BOOK_CHARGE = createSound("spell_book_charge");
    public static final SoundEvent SPELL_CAST = createSound("spell_cast");
    public static final SoundEvent SPELL_FAIL = createSound("spell_fail");
    public static final SoundEvent SPELL_CHARGE_SHORT = createSound("spell_charge_short");
    public static final SoundEvent SPELL_CHARGE_MEDIUM = createSound("spell_charge_medium");
    public static final SoundEvent SPELL_NO_MORE_USES = createSound("spell_no_more_uses");

    // ambience
    public static final SoundEvent AMBIENCE_CAVE_LONG = createSound("ambience.cave.long");
    public static final SoundEvent AMBIENCE_CAVE_SHORT = createSound("ambience.cave.short");
    public static final SoundEvent AMBIENCE_CRYSTALS_SHORT = createSound("ambience.crystals.short");
    public static final SoundEvent AMBIENCE_DEEP_LONG = createSound("ambience.deep.long");
    public static final SoundEvent AMBIENCE_DEEP_SHORT = createSound("ambience.deep.short");
    public static final SoundEvent AMBIENCE_DESERT_DAY_LONG = createSound("ambience.desert.day_long");
    public static final SoundEvent AMBIENCE_DESERT_DAY_SHORT = createSound("ambience.desert.day_short");
    public static final SoundEvent AMBIENCE_DESERT_NIGHT_LONG = createSound("ambience.desert.night_long");
    public static final SoundEvent AMBIENCE_DESERT_NIGHT_SHORT = createSound("ambience.desert.night_short");
    public static final SoundEvent AMBIENCE_EXTREME_HILLS_DAY_LONG = createSound("ambience.extreme_hills.day_long");
    public static final SoundEvent AMBIENCE_EXTREME_HILLS_NIGHT_LONG = createSound("ambience.extreme_hills.night_long");
    public static final SoundEvent AMBIENCE_FOREST_DAY_LONG = createSound("ambience.forest.day_long");
    public static final SoundEvent AMBIENCE_FOREST_NIGHT_LONG = createSound("ambience.forest.night_long");
    public static final SoundEvent AMBIENCE_ICY_DAY_LONG = createSound("ambience.icy.day_long");
    public static final SoundEvent AMBIENCE_ICY_NIGHT_LONG = createSound("ambience.icy.night_long");
    public static final SoundEvent AMBIENCE_MINESHAFT_SHORT = createSound("ambience.mineshaft.short");
    public static final SoundEvent AMBIENCE_NETHER_LONG = createSound("ambience.nether.long");
    public static final SoundEvent AMBIENCE_NETHER_SHORT = createSound("ambience.nether.short");
    public static final SoundEvent AMBIENCE_PLAINS_DAY_LONG = createSound("ambience.plains.day_long");
    public static final SoundEvent AMBIENCE_PLAINS_NIGHT_LONG = createSound("ambience.plains.night_long");
    public static final SoundEvent AMBIENCE_SAVANNA_DAY_LONG = createSound("ambience.savanna.day_long");
    public static final SoundEvent AMBIENCE_SAVANNA_NIGHT_LONG = createSound("ambience.savanna.night_long");
    public static final SoundEvent AMBIENCE_SWAMP_DAY_LONG = createSound("ambience.swamp.day_long");
    public static final SoundEvent AMBIENCE_SWAMP_NIGHT_LONG = createSound("ambience.swamp.night_long");
    public static final SoundEvent AMBIENCE_TAIGA_DAY_LONG = createSound("ambience.taiga.day_long");
    public static final SoundEvent AMBIENCE_TAIGA_NIGHT_LONG = createSound("ambience.taiga.night_long");
    public static final SoundEvent AMBIENCE_HIGH = createSound("ambience.high");

    public static SoundEvent createSound(String name)
    {
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(res).setRegistryName(res);
        soundsToRegister.add(sound);
        return sound;
    }

    public static void init()
    {
        soundsToRegister.forEach(RegistryHandler::registerSound);
    }
}
