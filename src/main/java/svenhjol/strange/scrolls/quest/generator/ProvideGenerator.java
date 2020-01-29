package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Provide;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class ProvideGenerator extends BaseGenerator
{
    public ProvideGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getProvide();

        for (String key : def.keySet()) {
            ItemStack stack = getItemFromKey(key);
            if (stack == null) continue;

            int count = getCountFromValue(def.get(key), false);

            Condition<Provide> condition = Condition.factory(Provide.class, quest);
            condition.getDelegate().setStack(stack).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
