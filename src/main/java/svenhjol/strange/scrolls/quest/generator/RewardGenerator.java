package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Reward;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.runestones.module.StoneCircles;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class RewardGenerator extends BaseGenerator
{
    public static final String ITEMS = "items";
    public static final String XP = "xp";
    public static final String COUNT = "count";
    public static final String STONE_CIRCLE_TOTEM = "StoneCircleTotem";
    public static final ArrayList<String> SPECIAL_ITEMS = new ArrayList<>(Arrays.asList(
        STONE_CIRCLE_TOTEM
    ));

    public RewardGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, Map<String, String>> def = definition.getRewards();
        if (def.isEmpty()) return;

        Condition<Reward> condition = Condition.factory(Reward.class, quest);
        Reward reward = condition.getDelegate();

        if (def.containsKey(ITEMS)) {
            Map<String, String> items = def.get(ITEMS);

            for (String stackName : items.keySet()) {
                ItemStack stack;

                if (SPECIAL_ITEMS.contains(stackName)) {
                    stack = getSpecialItemReward(stackName);
                    if (stack == null) continue;
                } else {
                    ResourceLocation res = ResourceLocation.tryCreate(stackName);
                    if (res == null) continue;
                    Item item = ForgeRegistries.ITEMS.getValue(res);
                    if (item == null) continue;

                    stack = new ItemStack(item);
                }

                int count = Integer.parseInt(items.get(stackName));
                count = multiplyDistance(count);

                reward.addItem(stack, count);
            }
        }

        if (def.containsKey(XP)) {
            Map<String, String> xp = def.get(XP);
            if (xp.containsKey(COUNT)) {
                int count = Integer.parseInt(xp.get(COUNT));
                count = multiplyDistance(count);

                reward.setXP(count);
            }
        }

        quest.getCriteria().addCondition(condition);
    }

    @Nullable
    private ItemStack getSpecialItemReward(String item)
    {
        switch (item) {
            case STONE_CIRCLE_TOTEM:
                if (world == null) return null;
                ItemStack totem = new ItemStack(TotemOfReturning.item);
                BlockPos pos = world.findNearestStructure(StoneCircles.NAME, Runestones.getOuterPos(world, world.rand), 1000, true);

                TotemOfReturningItem.setPos(totem, pos.add(0, world.getSeaLevel(), 0));
                totem.setDisplayName(new TranslationTextComponent("item.strange.quest_reward_totem"));

                return totem;

            default:
                break;
        }

        return null;
    }
}
