package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Mine;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Map;

public class MineGenerator extends BaseGenerator {
    public MineGenerator(World world, BlockPos pos, IQuest quest, Definition definition) {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate() {
        Map<String, String> def = definition.getMine();

        for (String key : def.keySet()) {
            Block block = getBlockFromKey(key);
            if (block == null) continue;

            int count = getCountFromValue(def.get(key), true);

            Condition<Mine> condition = Condition.factory(Mine.class, quest);
            condition.getDelegate().setBlock(block).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }
}
