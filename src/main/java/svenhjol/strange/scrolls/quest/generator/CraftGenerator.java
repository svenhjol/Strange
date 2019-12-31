package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Craft;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class CraftGenerator extends BaseGenerator
{
    public CraftGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getCraft();

        for (String key : def.keySet()) {
            ResourceLocation res = new ResourceLocation(key);
            Item item = ForgeRegistries.ITEMS.getValue(res);
            if (item == null) continue;
            int count = Integer.parseInt(def.get(key));

            // amount increases based on distance
            count = multiplyValue(count);

            Condition<Craft> condition = Condition.factory(Craft.class, quest);
            condition.getDelegate().setStack(new ItemStack(item)).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
