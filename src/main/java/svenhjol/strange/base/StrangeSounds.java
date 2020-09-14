package svenhjol.strange.base;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.Strange;

public class StrangeSounds {
    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");

    public static SoundEvent createSound(String name) {
        Identifier res = new Identifier(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(res);
        Registry.register(Registry.SOUND_EVENT, res, sound);
        return sound;
    }
}
