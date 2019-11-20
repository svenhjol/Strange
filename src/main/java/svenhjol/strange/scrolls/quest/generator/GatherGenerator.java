package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Gather;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class GatherGenerator extends BaseGenerator
{
    public GatherGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getGather();

        for (String key : def.keySet()) {
            ResourceLocation res = ResourceLocation.tryCreate(key);
            if (res == null) continue;

            Item item = ForgeRegistries.ITEMS.getValue(res);
            if (item == null) continue;

            ItemStack stack = new ItemStack(item);
            int count = Integer.parseInt(def.get(key));

            // amount increases based on distance
            count = multiplyDistance(count);

            Condition<Gather> condition = Condition.factory(Gather.class, quest);
            condition.getDelegate().setStack(stack).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
