package svenhjol.strange.module.quests.helper;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import svenhjol.charm.helper.LootHelper;
import svenhjol.charm.module.inventory_tidying.InventoryTidyingHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class QuestDefinitionHelper {
    public static final String TAG_CHANCE = "chance";
    public static final String TAG_COUNT = "count";
    public static final String TAG_ENCHANTED = "enchanted";
    public static final String TAG_ENCHANTMENTS = "enchantments";
    public static final String TAG_ENCHANTMENT_LEVEL = "enchantment_level";
    public static final String TAG_IS_TREASURE = "is_treasure";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_LIMIT = "limit";
    public static final String TAG_LOOT = "loot";
    public static final String TAG_NAME = "name";
    public static final String TAG_POOL = "pool";
    public static final String TAG_TABLE = "table";

    public static final List<Enchantment> FIXED_ENCHANTS;

    public static List<ItemStack> parseItems(ServerPlayer player, Map<String, Map<String, String>> definitionMap, int listLimit) {
        return parseItems(player, definitionMap, listLimit, 0.0F);
    }

    public static List<ItemStack> parseItems(ServerPlayer player, Map<String, Map<String, String>> definitionMap, int listLimit, float difficulty) {
        ServerLevel serverLevel = (ServerLevel)player.level;
        List<ItemStack> stacks = new ArrayList<>();

        for (String itemId : definitionMap.keySet()) {
            ItemStack stack;
            Map<String, String> props = definitionMap.get(itemId);
            List<String> itemIds = new ArrayList<>();
            Random random = player.getRandom();

            float chance;
            int count;
            int level;

            if (difficulty > 0.0F) {
                count = getScaledCountFromValue(props.get(TAG_COUNT), 1, difficulty, random);
                chance = getScaledChanceFromValue(props.get(TAG_CHANCE), 1.0F, difficulty);
                level = getScaledCountFromValue(props.get(TAG_ENCHANTMENT_LEVEL), 5, difficulty, random);
            } else {
                count = getCountFromValue(props.get(TAG_COUNT), 1, random);
                chance = getChanceFromValue(props.get(TAG_CHANCE), 1.0F);
                level = getCountFromValue(props.get(TAG_ENCHANTMENT_LEVEL), 5, random);
            }

            int limit = Integer.parseInt(props.getOrDefault(TAG_LIMIT, "1"));
            boolean isTreasure = Boolean.parseBoolean(props.getOrDefault(TAG_IS_TREASURE, "false"));
            boolean enchanted = Boolean.parseBoolean(props.getOrDefault(TAG_ENCHANTED, "false"));

            String items = props.getOrDefault(TAG_ITEMS, "");
            String name = props.getOrDefault(TAG_NAME, "");
            String enchantments = props.getOrDefault(TAG_ENCHANTMENTS, "");

            // if no chance for this item to generate, skip
            if (random.nextFloat() > chance) continue;

            // if count is zero, skip
            if (count <= 0)
                continue;

            if (itemId.startsWith(TAG_LOOT)) {
                // parse multiple items from a specified loot table (up to count)
                if (!props.containsKey(TAG_TABLE)) continue;

                // get the loot table ID and then load the lootmanager to generate a list of loot items
                ResourceLocation tableId = LootHelper.getLootTable(props.get(TAG_TABLE), BuiltInLootTables.SIMPLE_DUNGEON);

                LootTable table = serverLevel.getServer().getLootTables().get(tableId);
                List<ItemStack> list = table.getRandomItems((new LootContext.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withRandom(random)
                    .create(LootContextParamSets.CHEST)));

                if (list.isEmpty()) continue;

                // filter out empty items and shuffle loot items, fetching up to count
                List<ItemStack> lootItems = list.stream().filter(item -> !item.isEmpty()).collect(Collectors.toList());
                Collections.shuffle(lootItems, random);

                for (int i = 0; i < limit; i++) {
                    if (lootItems.size() > i) {
                        stacks.add(lootItems.get(i));
                    }
                }
                continue;

            } else if (itemId.startsWith(TAG_POOL)) {
                // parse multiple items from a specified pool list (up to limit)
                if (items.isEmpty()) continue;

                if (items.contains(",")) {
                    List<String> poolItems = splitByComma(items);
                    Collections.shuffle(poolItems);
                    itemIds.addAll(poolItems.subList(0, Math.min(poolItems.size(), limit)));
                } else {
                    itemIds.add(items);
                }

            } else {
                // just add the specified item id to parseable list
                itemIds.add(itemId);
            }

            // itemIds contains a list of all parseable items for this json entry
            for (String s : itemIds) {
                String parseableItemId = splitOptional(s, random);

                // try and parse a minecraft/modded item
                Optional<Item> optItem = Registry.ITEM.getOptional(new ResourceLocation(parseableItemId));
                if (optItem.isEmpty()) continue;

                Item item = optItem.get();
                stack = new ItemStack(item);

                // try adding custom name to item
                if (!name.isEmpty()) {
                    if (name.contains(".")) {
                        // shitty test for lang key
                        stack.setHoverName(new TranslatableComponent(name));
                    } else {
                        stack.setHoverName(new TextComponent(name));
                    }
                }

                if (stack.getMaxStackSize() < count) {
                    // separate stacks up to the count level
                    for (int i = 0; i < count; i++) {
                        stack = new ItemStack(item);

                        // add enchantments to each stack separately
                        if (enchanted) {
                            tryEnchant(stack, enchantments, level, isTreasure, random);
                        }

                        stacks.add(stack);
                    }
                } else {
                    // single stack with count as stacksize
                    stack.setCount(count);

                    if (enchanted) {
                        tryEnchant(stack, enchantments, level, isTreasure, random);
                    }

                    stacks.add(stack);
                }
            }
        }

        // if any stacks can be combined, do that here
        InventoryTidyingHandler.mergeStacks(stacks);

        // filter out dupe items
        List<Item> uniqueItems = new ArrayList<>();
        stacks = stacks.stream().filter(s -> {
            Item item = s.getItem();
            if (uniqueItems.contains(item)) {
                return false;
            }
            uniqueItems.add(item);
            return true;
        }).collect(Collectors.toList());

        // if more than limit, shuffle the set and return sublist
        if (stacks.size() > listLimit) {
            Collections.shuffle(stacks);
            return stacks.subList(0, listLimit);
        }

        return stacks;
    }

    @Nullable
    public static ResourceLocation getEntityIdFromKey(String key) {
        return getEntityIdFromKey(key, new Random());
    }

    @Nullable
    public static ResourceLocation getEntityIdFromKey(String key, Random random) {
        key = splitOptional(key, random);
        return ResourceLocation.tryParse(key);
    }

    public static int getCountFromValue(String value, int fallback) {
        return getCountFromValue(value, fallback, new Random());
    }

    public static int getCountFromValue(String value, int fallback, Random random) {
        int count;

        try {
            if (value.contains("!")) {
                return Integer.parseInt(value.replace("!", ""));
            }

            if (value.contains("-")) {
                String[] split = value.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                count = random.nextInt(Math.max(2, max - min)) + min;
            } else if (!value.isEmpty()) {
                count = Integer.parseInt(value);
            } else {
                count = fallback;
            }

        } catch (Exception e) {
            count = fallback;
        }

        return count;
    }

    public static int getScaledCountFromValue(String value, int fallback, float rarity, Random random) {
        int count = getCountFromValue(value, fallback, random);
        return Math.round(count * rarity);
    }

    public static float getChanceFromValue(String value, float fallback) {
        float chance;

        try {
            if (value.contains("!")) {
                return Float.parseFloat(value.replace("!", ""));
            }

            if (!value.isEmpty()) {
                chance = Float.parseFloat(value);
            } else {
                chance = fallback;
            }

        } catch (Exception e) {
            chance = fallback;
        }

        return chance;
    }

    public static float getScaledChanceFromValue(String value, float fallback, float difficulty) {
        float chance = getChanceFromValue(value, fallback);
        return chance + 0.1F * difficulty;
    }

    public static String splitOptional(String key) {
        return splitOptional(key, new Random());
    }

    public static String splitOptional(String key, Random random) {
        if (key.contains("|")) {
            String[] split = key.split("\\|");
            key = split[random.nextInt(split.length)];
        }

        key = key.trim();
        return key;
    }

    public static List<String> splitByComma(String key) {
        List<String> split = new ArrayList<>();

        if (key.contains(",")) {
            split.addAll(Arrays.asList(key.split(",")));
        } else {
            split.add(key);
        }

        return split;
    }

    private static void tryEnchant(ItemStack stack, String enchantments, int enchantmentLevel, boolean isTreasure, Random random) {
        List<String> specificEnchantments = new ArrayList<>();

        if (!enchantments.isEmpty()) {
            specificEnchantments = splitByComma(splitOptional(enchantments, random));
        }

        if (!stack.isEnchanted()) {
            if (!specificEnchantments.isEmpty()) {

                // if a set of enchantments has been defined, apply them with a random level (using enchantmentLevel)
                Map<Enchantment, Integer> toApply = new HashMap<>();
                for (String e : specificEnchantments) {
                    int level = 0;

                    // enchantments can be specified as name#level
                    if (e.contains("#")) {
                        String[] split = e.split("#");
                        e = split[0];
                        level = Integer.parseInt(split[1]);
                    }

                    Optional<Enchantment> opt = Registry.ENCHANTMENT.getOptional(new ResourceLocation(e));
                    if (opt.isEmpty()) continue;
                    Enchantment enchantment = opt.get();

                    if (level == 0) {
                        level = Math.min(enchantment.getMaxLevel(), random.nextInt(enchantmentLevel) + 1);
                    }

                    toApply.put(enchantment, level);
                }

                EnchantmentHelper.setEnchantments(toApply, stack);

            } else if (stack.isEnchantable()) {

                // if the stack can be enchanted, just enchant randomly
                EnchantmentHelper.enchantItem(random, stack, enchantmentLevel, isTreasure);

            } else if (enchantmentLevel > 0) {

                // if the stack isn't naturally enchantable, select from the FIXED_ENCHANTS map
                List<Enchantment> fixedEnchants = new ArrayList<>(FIXED_ENCHANTS);
                Collections.shuffle(fixedEnchants);

                for (Enchantment enchantment : fixedEnchants) {
                    int level = Math.min(enchantment.getMaxLevel(), random.nextInt(enchantmentLevel) + 1);
                    stack.enchant(enchantment, level);
                    if (random.nextFloat() < 0.75F) break;
                }
            }
        }
    }

    static {
        FIXED_ENCHANTS = Arrays.asList(
            Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK,
            Enchantments.UNBREAKING,
            Enchantments.SHARPNESS,
            Enchantments.MOB_LOOTING,
            Enchantments.BLOCK_FORTUNE
        );
    }
}
