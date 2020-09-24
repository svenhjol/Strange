package svenhjol.strange.scroll.populator;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import svenhjol.strange.scroll.ScrollQuest;
import svenhjol.strange.scroll.ScrollDefinition;

import javax.annotation.Nullable;

public abstract class Populator {
    public static final float EPIC_CHANCE_BASE = 0.02F;
    public static final float RARE_CHANCE_BASE = 0.15F;
    public static final float UNCOMMON_CHANCE_BASE = 0.45F;
    public static final float COMMON_CHANCE_BASE = 0.75F;

    public static final String ENCHANTED_LABEL = "[enchanted]";
    public static final String EPIC_LABEL = "[epic]";
    public static final String RARE_LABEL = "[rare]";
    public static final String COMMON_LABEL = "[common]";
    public static final String UNCOMMON_LABEL = "[uncommon]";

    protected final World world;
    protected final BlockPos pos;
    protected final ScrollQuest scrollQuest;
    protected final ScrollDefinition definition;

    public Populator(World world, BlockPos pos, ScrollQuest scrollQuest, ScrollDefinition definition) {
        this.world = world;
        this.pos = pos;
        this.scrollQuest = scrollQuest;
        this.definition = definition;
    }

    public abstract void populate();

    @Nullable
    public ItemStack getItemFromKey(String key) {
        ItemStack stack;
        key = splitOptionalRandomly(key);

        boolean doEnchant = key.contains(ENCHANTED_LABEL);
        boolean isRare = key.contains(RARE_LABEL) || key.contains(EPIC_LABEL);

        if (key.contains(ENCHANTED_LABEL))
            key = key.replace(ENCHANTED_LABEL, "");

        key = filterRarity(key);
        if (key == null)
            return null;

        Identifier itemId = Identifier.tryParse(key);
        if (itemId == null)
            return null;

        if (!Registry.ITEM.containsId(itemId))
            return null;

        Item item = Registry.ITEM.get(itemId);
        stack = new ItemStack(item);

        if (doEnchant && item.isEnchantable(stack))
            EnchantmentHelper.generateEnchantments(world.random, stack, isRare ? 15 : 5, isRare);

        return stack;
    }

    @Nullable
    public Identifier getEntityResFromKey(String key) {
        key = splitOptionalRandomly(key);

        key = filterRarity(key);
        if (key == null)
            return null;

        return Identifier.tryParse(key);
    }

    @Nullable
    public String filterRarity(String key) {
        if (key.contains(EPIC_LABEL)) {
            // epic items only have a small chance to pass, boosted by quest value
            if (world.random.nextFloat() > (EPIC_CHANCE_BASE + (0.02F * scrollQuest.getRarity()))) return null;
            return key.replace(EPIC_LABEL, "");

        } else if (key.contains(RARE_LABEL)) {
            // rare items only have a small chance to pass, boosted by quest value
            if (world.random.nextFloat() > (RARE_CHANCE_BASE + (0.02F * scrollQuest.getRarity()))) return null;
            return key.replace(RARE_LABEL, "");

        } else if (key.contains(UNCOMMON_LABEL)) {
            // uncommon items pass sometimes
            if (world.random.nextFloat() > (UNCOMMON_CHANCE_BASE + (0.05F * scrollQuest.getRarity()))) return null;
            return key.replace(UNCOMMON_LABEL, "");

        } else if (key.contains(COMMON_LABEL)) {
            // common items pass often
            if (world.random.nextFloat() > (COMMON_CHANCE_BASE + (0.05F * scrollQuest.getRarity()))) return null;
            return key.replace(COMMON_LABEL, "");
        }

        return key;
    }

    public int getCountFromValue(String value, boolean scale) {
        int count;

        if (value.contains("!"))
            return Integer.parseInt(value.replace("!", ""));

        if (value.contains("-")) {
            String[] split = value.split("-");
            count = world.random.nextInt(Integer.parseInt(split[1])) + Integer.parseInt(split[0]);
        } else {
            count = Integer.parseInt(value);
        }

        return scale ? multiplyValue(count) : count;
    }

    public String splitOptionalRandomly(String key) {
        if (key.contains("|")) {
            String[] split = key.split("\\|");
            key = split[world.random.nextInt(split.length)];
        }

        key = key.trim();
        return key;
    }

    public int multiplyValue(int original) {
        return original * scrollQuest.getRarity();
    }
}
