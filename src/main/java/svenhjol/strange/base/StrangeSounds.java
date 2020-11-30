package svenhjol.strange.base;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.strange.Strange;

public class StrangeSounds {
    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");
    public static final SoundEvent SCREENSHOT = createSound("screenshot");

    public static SoundEvent createSound(String name) {
        Identifier id = new Identifier(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(id);
        RegistryHandler.sound(id, sound);
        return sound;
    }
}
