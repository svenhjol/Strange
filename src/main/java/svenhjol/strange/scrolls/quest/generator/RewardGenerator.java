package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.runestones.module.StoneCircles;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Reward;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.spells.helper.SpellsHelper;
import svenhjol.strange.spells.item.MoonstoneItem;
import svenhjol.strange.spells.item.SpellBookItem;
import svenhjol.strange.spells.module.Moonstones;
import svenhjol.strange.spells.module.SpellBooks;
import svenhjol.strange.spells.module.Spells;
import svenhjol.strange.spells.spells.Spell;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.traveljournal.module.TravelJournal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class RewardGenerator extends BaseGenerator
{
    public static final String ITEMS = "items";
    public static final String XP = "xp";
    public static final String COUNT = "count";

    // special item tags
    public static final String STONE_CIRCLE_TOTEM = "StoneCircleTotem";
    public static final String RANDOM_ENCHANTED_BOOK = "RandomEnchantedBook";
    public static final String RANDOM_RARE_ENCHANTED_BOOK = "RandomRareEnchantedBook";
    public static final String RANDOM_SPELL_BOOK = "RandomSpellBook";
    public static final String RANDOM_RARE_SPELL_BOOK = "RandomRareSpellBook";
    public static final String RANDOM_MOONSTONE = "RandomMoonstone";
    public static final String TRAVEL_JOURNAL = "TravelJournal";

    public static final ArrayList<String> SPECIAL_ITEMS = new ArrayList<>(Arrays.asList(
        STONE_CIRCLE_TOTEM,
        RANDOM_ENCHANTED_BOOK,
        RANDOM_SPELL_BOOK,
        RANDOM_RARE_ENCHANTED_BOOK,
        RANDOM_RARE_SPELL_BOOK,
        RANDOM_MOONSTONE,
        TRAVEL_JOURNAL
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
                int count = Integer.parseInt(items.get(stackName));

                if (SPECIAL_ITEMS.contains(stackName)) {
                    stack = getSpecialItemReward(stackName);
                    if (stack == null) continue;
                } else {
                    ResourceLocation res = ResourceLocation.tryCreate(stackName);
                    if (res == null) continue;
                    Item item = ForgeRegistries.ITEMS.getValue(res);
                    if (item == null) continue;

                    stack = new ItemStack(item);
                    count = multiplyValue(count);
                }

                reward.addItem(stack, count);
            }
        }

        if (def.containsKey(XP)) {
            Map<String, String> xp = def.get(XP);
            if (xp.containsKey(COUNT)) {
                int count = Integer.parseInt(xp.get(COUNT));
                count = multiplyValue(count);

                reward.setXP(count);
            }
        }

        quest.getCriteria().addCondition(condition);
    }

    @Nullable
    private ItemStack getSpecialItemReward(String item)
    {
        if (world == null) return null;

        Random rand = world.rand;
        ItemStack out = null;
        boolean rare;

        switch (item) {
            case STONE_CIRCLE_TOTEM:
                if (Strange.loader.hasModule(Runestones.class) && Strange.loader.hasModule(TotemOfReturning.class)) {
                    out = new ItemStack(TotemOfReturning.item);
                    BlockPos pos = world.findNearestStructure(StoneCircles.NAME, Runestones.getOuterPos(world, world.rand), 1000, true);
                    if (pos == null) return null;

                    TotemOfReturningItem.setPos(out, pos.add(0, world.getSeaLevel(), 0));
                    out.setDisplayName(new TranslationTextComponent("item.strange.quest_reward_totem"));
                }
                break;

            case RANDOM_ENCHANTED_BOOK:
            case RANDOM_RARE_ENCHANTED_BOOK:
                out = new ItemStack(Items.BOOK);
                rare = item.equals(RANDOM_RARE_ENCHANTED_BOOK);
                out = EnchantmentHelper.addRandomEnchantment(rand, out, rand.nextInt(15) + (rare ? 15 : 1), rare);
                break;

            case RANDOM_SPELL_BOOK:
            case RANDOM_RARE_SPELL_BOOK:
                if (Strange.loader.hasModule(Spells.class)) {
                    out = new ItemStack(SpellBooks.book);
                    rare = item.equals(RANDOM_RARE_SPELL_BOOK);
                    Spell spell = SpellsHelper.getRandomSpell(rand, rare);
                    if (spell == null) return null;
                    SpellBookItem.putSpell(out, spell);
                }
                break;

            case RANDOM_MOONSTONE:
                if (Strange.loader.hasModule(Moonstones.class)) {
                    out = new ItemStack(Moonstones.item);
                    Spell spell = SpellsHelper.getRandomSpell(rand, true);
                    if (spell == null) return null;
                    MoonstoneItem.putSpell(out, spell);
                }
                break;

            case TRAVEL_JOURNAL:
                if (Strange.loader.hasModule(TravelJournal.class)) {
                    out = new ItemStack(TravelJournal.item);
                }
                break;

            default:
                break;
        }

        return out;
    }
}
