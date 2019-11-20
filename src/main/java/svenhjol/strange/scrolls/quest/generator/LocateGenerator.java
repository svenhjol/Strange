package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Locate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class LocateGenerator extends BaseGenerator
{
    public LocateGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        List<String> def = definition.getLocate();

        for (String key : def) {
            ResourceLocation res = ResourceLocation.tryCreate(key);
            if (res == null) continue;

            Item item = ForgeRegistries.ITEMS.getValue(res);
            if (item == null) continue;

            ItemStack stack = new ItemStack(item);

            Condition<Locate> condition = Condition.factory(Locate.class, quest);
            condition.getDelegate().setStack(stack);
            quest.getCriteria().addCondition(condition);
        }
    }
}
