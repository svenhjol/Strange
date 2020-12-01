package svenhjol.strange.base;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StrangeSounds {
    public static Map<Identifier, SoundEvent> REGISTER = new HashMap<>();

    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");
    public static final SoundEvent SCREENSHOT = createSound("screenshot");

    public static void init() {
        REGISTER.forEach(RegistryHandler::sound);
    }

    public static SoundEvent createSound(String name) {
        Identifier id = new Identifier(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(id);
        REGISTER.put(id, sound);
        return sound;
    }
}
