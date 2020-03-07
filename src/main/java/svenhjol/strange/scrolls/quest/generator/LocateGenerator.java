package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Locate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class LocateGenerator extends BaseGenerator {
    public LocateGenerator(World world, BlockPos pos, IQuest quest, Definition definition) {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate() {
        List<String> def = definition.getLocate();

        for (String key : def) {
            ItemStack stack = getItemFromKey(key);
            if (stack == null) continue;

            Condition<Locate> condition = Condition.factory(Locate.class, quest);
            condition.getDelegate().setStack(stack);
            quest.getCriteria().addCondition(condition);
        }
    }
}
