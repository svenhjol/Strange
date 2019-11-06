package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.Mine;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class MineGenerator extends BaseGenerator
{
    public MineGenerator(World world, BlockPos pos, IQuest quest, Generator.Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getMine();

        for (String key : def.keySet()) {
            int count = Integer.parseInt(def.get(key));

            // amount increases based on distance
            count = multiplyDistance(count);

            Condition<Mine> condition = Condition.factory(Mine.class, quest);
            condition.getDelegate().setBlock(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key))).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
