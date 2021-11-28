package svenhjol.strange.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StrangeSounds {
    public static Map<ResourceLocation, SoundEvent> REGISTER = new HashMap<>();

    public static final SoundEvent FERMENT = createSound("ferment");
    public static final SoundEvent COOKING_POT = createSound("cooking_pot");
    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");
    public static final SoundEvent SCREENSHOT = createSound("screenshot");

    public static void init() {
        REGISTER.forEach(CommonRegistry::sound);
    }

    public static SoundEvent createSound(String name) {
        ResourceLocation id = new ResourceLocation(Strange.MOD_ID, name);
        SoundEvent sound = new SoundEvent(id);
        REGISTER.put(id, sound);
        return sound;
    }
}
