package svenhjol.strange.module.scrolls.populator;

import net.minecraft.core.BlockPos;
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
import svenhjol.strange.module.scrolls.ScrollDefinition;
import svenhjol.strange.module.scrolls.ScrollDefinitionHelper;
import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.nbt.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BasePopulator {
    public static final String CHANCE_NBT = "chance";
    public static final String COUNT_NBT = "count";
    public static final String ENCHANTED_NBT = "enchanted";
    public static final String ENCHANTMENTS_NBT = "enchantments";
    public static final String ENCHANTMENT_LEVEL_NBT = "enchantment_level";
    public static final String ENCHANTMENT_TREASURE_NBT = "enchantment_treasure";
    public static final String ITEMS_NBT = "items";
    public static final String LIMIT_NBT = "limit";
    public static final String LOOT_NBT = "loot";
    public static final String NAME_NBT = "name";
    public static final String POOL_NBT = "pool";
    public static final String TABLE_NBT = "table";

    public static final List<Enchantment> FIXED_ENCHANTS = new ArrayList<>();

    protected final ServerPlayer player;
    protected final ServerLevel world;
    protected final BlockPos pos;
    protected final Quest quest;
    protected final ScrollDefinition definition;

    public BasePopulator(ServerPlayer player, Quest quest) {
        this.player = player;
        this.world = (ServerLevel)player.level;
        this.pos = player.blockPosition();
        this.quest = quest;
        this.definition = Scrolls.getDefinition(quest.getDefinition());

        FIXED_ENCHANTS.addAll(Arrays.asList(
            Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK,
            Enchantments.UNBREAKING,
            Enchantments.SHARPNESS,
            Enchantments.MOB_LOOTING,
            Enchantments.BLOCK_FORTUNE
        ));
    }

    public abstract void populate();

    public ItemStack getMap() {
        return ItemStack.EMPTY;
    }

    public List<ItemStack> parseItems(Map<String, Map<String, String>> map, int listLimit, boolean scale) {
        List<ItemStack> stacks = new ArrayList<>();

        for (String itemId : map.keySet()) {
            ItemStack stack;
            Map<String, String> props = map.get(itemId);
            List<String> itemIds = new ArrayList<>();

            // get all values from item props
            int count = getCountFromValue(props.get(COUNT_NBT), 1, scale);
            int limit = Integer.parseInt(props.getOrDefault(LIMIT_NBT, "1"));
            float chance = getChanceFromValue(props.get(CHANCE_NBT), 1.0F, scale);
            int enchantmentLevel = getCountFromValue(props.get(ENCHANTMENT_LEVEL_NBT), 5, scale);
            boolean enchantmentTreasure = Boolean.parseBoolean(props.getOrDefault(ENCHANTMENT_TREASURE_NBT, "false"));
            boolean enchanted = Boolean.parseBoolean(props.getOrDefault(ENCHANTED_NBT, "false"));
            String items = props.getOrDefault(ITEMS_NBT, "");
            String enchantments = props.getOrDefault(ENCHANTMENTS_NBT, "");
            String name = props.getOrDefault(NAME_NBT, "");

            // if no chance for this item to generate, skip
            if (world.random.nextFloat() > chance)
                continue;

            // if count is zero, skip
            if (count <= 0)
                continue;

            if (itemId.startsWith(LOOT_NBT)) {
                // parse multiple items from a specified loot table (up to count)
                if (!props.containsKey(TABLE_NBT))
                    continue;

                // get the loot table ID and then load the lootmanager to generate a list of loot items
                ResourceLocation tableId = LootHelper.getLootTable(props.get(TABLE_NBT), BuiltInLootTables.SIMPLE_DUNGEON);

                LootTable table = world.getServer().getLootTables().get(tableId);
                List<ItemStack> list = table.getRandomItems((new LootContext.Builder(world)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withRandom(world.random)
                    .create(LootContextParamSets.CHEST)));

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

            } else if (itemId.startsWith(POOL_NBT)) {
                // parse multiple items from a specified pool list (up to limit)
                if (items.isEmpty())
                    continue;

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
                String parseableItemId = splitOptionalRandomly(s);

                // try and parse a minecraft/modded item
                Optional<Item> optionalItem = Registry.ITEM.getOptional(new ResourceLocation(parseableItemId));
                if (optionalItem.isEmpty())
                    continue;

                Item item = optionalItem.get();
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
    public ResourceLocation getEntityIdFromKey(String key) {
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

    public List<String> splitByComma(String key) {
        return ScrollDefinitionHelper.splitByComma(key);
    }

    private void tryEnchant(ItemStack stack, String enchantments, int enchantmentLevel, boolean isTreasure) {
        Random random = world.random;
        List<String> specificEnchantments = new ArrayList<>();

        if (!enchantments.isEmpty())
            specificEnchantments = splitByComma(splitOptionalRandomly(enchantments));

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

                    Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(e));
                    if (optionalEnchantment.isEmpty())
                        continue;

                    Enchantment enchantment = optionalEnchantment.get();

                    if (level == 0)
                        level = Math.min(enchantment.getMaxLevel(), random.nextInt(enchantmentLevel) + 1);

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
                    if (random.nextFloat() < 0.75F)
                        break;
                }
            }
        }
    }
}
