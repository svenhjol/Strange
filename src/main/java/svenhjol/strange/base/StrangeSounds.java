package svenhjol.strange.base;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import svenhjol.strange.Strange;

public class StrangeSounds
{
    public static final SoundEvent ATTRACTED = createSound("attracted");
    public static final SoundEvent QUEST_ACTION_COMPLETE = createSound("quest_action_complete");
    public static final SoundEvent QUEST_ACTION_COUNT = createSound("quest_action_count");
    public static final SoundEvent RUNESTONE_TRAVEL = createSound("runestone_travel");
    public static final SoundEvent SCREENSHOT = createSound("screenshot");

    public static SoundEvent createSound(String name)
    {
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, name);
        return new SoundEvent(res).setRegistryName(res);
    }
}
