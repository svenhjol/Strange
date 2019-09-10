package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.world.World;

public interface IGenerator
{
    IQuest generate(World world, IQuest quest, int tier);
}
