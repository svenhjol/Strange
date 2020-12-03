package svenhjol.strange.scrolls.populator;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.handler.InventoryTidyingHandler;
import svenhjol.strange.base.helper.ScrollDefinitionHelper;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Populator {
    public static final String COUNT = "count";
    public static final String LIMIT = "limit";
    public static final String NAME = "name";
    public static final String CHANCE = "chance";
    public static final String ENCHANTED = "enchanted";
    public static final String ENCHANTMENT_LEVEL = "enchantment_level";
    public static final String ENCHANTMENT_TREASURE = "enchantment_treasure";
    public static final String ENCHANTMENTS = "enchantments";
    public static final String ITEMS = "items";
    public static final String LOOT = "loot";
    public static final String POOL = "pool";
    public static final String TABLE = "table";

    private static final List<Enchantment> FORCED_ENCHANTS = new ArrayList<>(Arrays.asList(
        Enchantments.FIRE_ASPECT,
        Enchantments.KNOCKBACK,
        Enchantments.UNBREAKING,
        Enchantments.SHARPNESS,
        Enchantments.LOOTING,
        Enchantments.FORTUNE
    ));

    protected final ServerPlayerEntity player;
    protected final ServerWorld world;
    protected final BlockPos pos;
    protected final Quest quest;
    protected final JsonDefinition definition;

    public Populator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        this.player = player;
        this.world = (ServerWorld)player.world;
        this.pos = player.getBlockPos();
        this.quest = quest;
        this.definition = definition;
    }

    public abstract void populate();

    public List<ItemStack> parseItems(Map<String, Map<String, String>> map, int listLimit, boolean scale) {
        List<ItemStack> stacks = new ArrayList<>();

        for (String itemId : map.keySet()) {
            ItemStack stack;
            Map<String, String> props = map.get(itemId);
            List<String> itemIds = new ArrayList<>();

            // get all values from item props
            int count = getCountFromValue(props.get(COUNT), 1, scale);
            int limit = Integer.parseInt(props.getOrDefault(LIMIT, "1"));
            float chance = getChanceFromValue(props.get(CHANCE), 1.0F, scale);
            int enchantmentLevel = getCountFromValue(props.get(ENCHANTMENT_LEVEL), 5, scale);
            boolean enchantmentTreasure = Boolean.parseBoolean(props.getOrDefault(ENCHANTMENT_TREASURE, "false"));
            boolean enchanted = Boolean.parseBoolean(props.getOrDefault(ENCHANTED, "false"));
            String items = props.getOrDefault(ITEMS, "");
            String enchantments = props.getOrDefault(ENCHANTMENTS, "");
            String name = props.getOrDefault(NAME, "");

            // if no chance for this item to generate, skip
            if (world.random.nextFloat() > chance)
                continue;

            // if count is zero, skip
            if (count <= 0)
                continue;

            if (itemId.startsWith(LOOT)) {
                // parse multiple items from a specified loot table (up to count)
                if (!props.containsKey(TABLE))
                    continue;

                // get the loot table ID and then load the lootmanager to generate a list of loot items
                Identifier tableId = LootHelper.getLootTable(props.get(TABLE), LootTables.SIMPLE_DUNGEON_CHEST);

                LootTable table = world.getServer().getLootManager().getTable(tableId);
                List<ItemStack> list = table.generateLoot((new LootContext.Builder(world)
                    .parameter(LootContextParameters.THIS_ENTITY, player)
                    .parameter(LootContextParameters.ORIGIN, player.getPos())
                    .random(world.random)
                    .build(LootContextTypes.CHEST)));

                if (list.isEmpty())
                    continue;

                // filter out empty items and shuffle loot items, fetching up to count
                List<ItemStack> lootItems = list.stream().filter(item -> !item.isEmpty()).collect(Collectors.toList());
                Collections.shuffle(lootItems, world.random);

                for (int i = 0; i < limit; i++) {
                    if (lootItems.size() > i)
                        stacks.add(lootItems.get(i));
                }
                continue;

            } else if (itemId.startsWith(POOL)) {
                // parse multiple items from a specified pool list (up to limit)
                if (items.isEmpty())
                    continue;

                if (items.contains(",")) {
                    List<String> poolItems = Arrays.asList(items.split(","));
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
                String parseableItemId = splitOptionalRandomly(s);

                // try and parse a minecraft/modded item
                Optional<Item> optionalItem = Registry.ITEM.getOrEmpty(new Identifier(parseableItemId));
                if (!optionalItem.isPresent())
                    continue;

                Item item = optionalItem.get();
                stack = new ItemStack(item);

                // try adding custom name to item
                if (!name.isEmpty()) {
                    if (name.contains(".")) {
                        // shitty test for lang key
                        stack.setCustomName(new TranslatableText(name));
                    } else {
                        stack.setCustomName(new LiteralText(name));
                    }
                }

                if (stack.getMaxCount() < count) {
                    // separate stacks up to the count level
                    for (int i = 0; i < count; i++) {
                        stack = new ItemStack(item);

                        // add enchantments to each stack separately
                        if (enchanted)
                            tryEnchant(stack, enchantments, enchantmentLevel, enchantmentTreasure);

                        stacks.add(stack);
                    }
                } else {
                    // single stack with count as stacksize
                    stack.setCount(count);

                    if (enchanted)
                        tryEnchant(stack, enchantments, enchantmentLevel, enchantmentTreasure);

                    stacks.add(stack);
                }
            }
        }

        // if any stacks can be combined, do that here
        InventoryTidyingHandler.mergeInventory(stacks);


        // if more than limit, shuffle the set and return sublist
        if (stacks.size() > listLimit) {
            Collections.shuffle(stacks);
            return stacks.subList(0, listLimit);
        }

        return stacks;
    }

    @Nullable
    public Identifier getEntityIdFromKey(String key) {
        return ScrollDefinitionHelper.getEntityIdFromKey(key, world.random);
    }

    public int getCountFromValue(String value, int fallback, boolean scale) {
        return ScrollDefinitionHelper.getCountFromValue(value, fallback, quest.getRarity(), world.random, scale);
    }

    public float getChanceFromValue(String value, float fallback, boolean scale) {
        return ScrollDefinitionHelper.getChanceFromValue(value, fallback, quest.getRarity(), scale);
    }

    public String splitOptionalRandomly(String key) {
        return ScrollDefinitionHelper.splitOptionalRandomly(key, world.random);
    }

    private void tryEnchant(ItemStack stack, String enchantments, int enchantmentLevel, boolean treasure) {
        Random random = world.random;
        List<String> specificEnchantments = new ArrayList<>();

        if (!enchantments.isEmpty()) {
            if (enchantments.contains(",")) {
                specificEnchantments.addAll(Arrays.asList(enchantments.split(",")));
            } else {
                specificEnchantments.add(enchantments);
            }
        }

        if (!stack.hasEnchantments()) {
            if (!specificEnchantments.isEmpty()) {

                // if a set of enchantments has been defined, apply them with a random level (using enchantmentLevel)
                Map<Enchantment, Integer> toApply = new HashMap<>();
                for (String e : specificEnchantments) {
                    Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOrEmpty(new Identifier(e));
                    optionalEnchantment.ifPresent(enchantment -> {
                        int level = Math.min(enchantment.getMaxLevel(), random.nextInt(enchantmentLevel) + 1);
                        toApply.put(enchantment, level);
                    });
                }
                EnchantmentHelper.set(toApply, stack);

            } else if (stack.isEnchantable()) {

                // if the stack can be enchanted, just enchant randomly
                EnchantmentHelper.enchant(random, stack, enchantmentLevel, treasure);

            } else if (enchantmentLevel > 0) {

                // if the stack isn't naturally enchantable, force from the FORCED_ENCHANTS map
                List<Enchantment> forcedEnchants = new ArrayList<>(FORCED_ENCHANTS);
                Collections.shuffle(forcedEnchants);

                for (Enchantment enchantment : forcedEnchants) {
                    int level = Math.min(enchantment.getMaxLevel(), random.nextInt(enchantmentLevel) + 1);
                    stack.addEnchantment(enchantment, level);
                    if (random.nextFloat() < 0.75F)
                        break;
                }
            }
        }
    }
}
