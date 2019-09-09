package svenhjol.strange.scrolls.quest;

import net.minecraft.world.World;

public interface IGenerator
{
    IQuest generate(World world, IQuest quest, int tier);
}
