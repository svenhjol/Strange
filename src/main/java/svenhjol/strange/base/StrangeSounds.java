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

    public static final SoundEvent MUSIC_THARNA = createSound("music.tharna");
    public static final SoundEvent MUSIC_STEINN = createSound("music.steinn");
    public static final SoundEvent MUSIC_MUS = createSound("music.mus");
    public static final SoundEvent MUSIC_UNDIR = createSound("music.undir");
    public static final SoundEvent MUSIC_DISC = createSound("music_disc.strange");
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
