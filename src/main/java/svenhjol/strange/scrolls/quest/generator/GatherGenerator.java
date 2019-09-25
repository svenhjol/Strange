package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.condition.Gather;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class GatherGenerator extends BaseGenerator
{
    public GatherGenerator(World world, IQuest quest, Definition definition)
    {
        super(world, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getGather();

        for (String key : def.keySet()) {
            ResourceLocation res = new ResourceLocation(key);
            Item item = ForgeRegistries.ITEMS.getValue(res);
            if (item == null) continue;
            int count = definition.parseCount(def.get(key));

            Condition<Gather> condition = Condition.factory(Gather.class, quest);
            condition.getDelegate().setStack(new ItemStack(item)).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
