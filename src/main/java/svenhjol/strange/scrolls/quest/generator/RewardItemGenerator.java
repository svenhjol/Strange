package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.RewardItem;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.travelrunes.module.Runestones;
import svenhjol.strange.travelrunes.module.StoneCircles;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class RewardItemGenerator extends BaseGenerator
{
    public static final String TOTEM_DISTANT_STONE_CIRCLE = "TotemDistantStoneCircle";

    public static final ArrayList<String> SPECIAL_ITEM_REWARDS = new ArrayList<>(Arrays.asList(
        TOTEM_DISTANT_STONE_CIRCLE
    ));

    public RewardItemGenerator(World world, IQuest quest, Generator.Definition definition)
    {
        super(world, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, String> def = definition.getRewardItems();

        for (String key : def.keySet()) {
            ItemStack stack;

            if (SPECIAL_ITEM_REWARDS.contains(key)) {
                stack = getSpecialItemReward(key);
                if (stack == null) continue;
            } else {
                ResourceLocation res = new ResourceLocation(key);
                Item item = ForgeRegistries.ITEMS.getValue(res);
                if (item == null) continue;
                stack = new ItemStack(item);
            }

            int count = definition.parseCount(def.get(key));

            Condition<RewardItem> condition = Condition.factory(RewardItem.class, quest);
            condition.getDelegate().setStack(stack).setCount(count);
            quest.getCriteria().addCondition(condition);
        }
    }

    @Nullable
    private ItemStack getSpecialItemReward(String item)
    {
        switch (item) {
            case TOTEM_DISTANT_STONE_CIRCLE:
                if (world == null) return null;
                ItemStack totem = new ItemStack(TotemOfReturning.item);
                final BlockPos circlePos = world.findNearestStructure(StoneCircles.NAME, Runestones.getOuterPos(world.rand), 1000, true);

                if (circlePos != null) {
                    TotemOfReturningItem.setPos(totem, circlePos.add(0, 1, 0));
                    totem.setDisplayName(new StringTextComponent("Distant Stone Circle"));
                }

                return totem;

            default:
                break;
        }

        return null;
    }
}
