package svenhjol.strange.base;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.Charm;
import svenhjol.meson.MesonMod;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StrangeSounds {
    private static final Map<Identifier, SoundEvent> REGISTER = new HashMap<>();

    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");

    public static SoundEvent createSound(String name) {
        Identifier res = new Identifier(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(res);
        REGISTER.put(res, sound);
        return sound;
    }

    public static void init(MesonMod mod) {
        REGISTER.forEach((res, sound) -> {
            Registry.register(Registry.SOUND_EVENT, res, sound);
        });
    }
}
