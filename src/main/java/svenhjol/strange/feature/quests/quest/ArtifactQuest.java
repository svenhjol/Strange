package svenhjol.strange.feature.quests.quest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.feature.colored_glints.ColoredGlints;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.data.LinkedResourceList;
import svenhjol.strange.data.ResourceListManager;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.Requirement;

import java.util.*;

public class ArtifactQuest extends Quest {
    static final ResourceLocation DEFAULT_ARTIFACT_ITEMS = new ResourceLocation(Strange.ID, "default_artifact_items");
    static final String ARTIFACT_TAG = "artifact";
    static final String LOOT_TABLES_TAG = "loot_tables";

    ArtifactItem artifact;
    final List<ResourceLocation> lootTables = new ArrayList<>();

    @Override
    public List<? extends Requirement> requirements() {
        return List.of(artifact);
    }

    public List<ResourceLocation> lootTables() {
        return lootTables;
    }

    @Override
    public Optional<ItemStack> addToLootTable(ResourceLocation lootTableId, RandomSource random) {
        if (player == null) {
            return Optional.empty();
        }

        if (inProgress() && !lootTables.isEmpty()) {
            for (var lootTable : lootTables) {
                if (lootTable.equals(lootTableId)) {
                    // Check if the player is already carrying the artifact.
                    if (hasArtifact()) {
                        log().debug(getClass(), "Player already has the artifact, skipping artifact loot generation");
                        return Optional.empty();
                    }

                    var chance = 1.0d - villagerLevel() * 0.1d;
                    if (random.nextDouble() < chance) {
                        log().debug(getClass(), "Adding the artifact to the loot pool");
                        return Optional.of(artifact.item.copy());
                    } else {
                        log().debug(getClass(), "Did not pass chance check, skipping artifact loot generation");
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected void makeRequirements(ResourceManager manager, QuestDefinition definition) {
        var entries = ResourceListManager.entries(manager, "quests/artifact");

        // Populate the loot tables.
        var lootTableEntries = definition.pair(definition.artifactLootTables(), random());
        var lootTableEntry = lootTableEntries.getFirst();
        var lootTableAmount = lootTableEntries.getSecond();
        var lootTables = LinkedResourceList.load(entries.getOrDefault(lootTableEntry, new LinkedList<>()));

        if (lootTables.isEmpty()) {
            throw new RuntimeException("Loot table list is empty");
        }

        Collections.shuffle(lootTables);
        for (var i = 0; i < Math.min(lootTableAmount, lootTables.size()); i++) {
            this.lootTables.add(lootTables.get(i));
        }

        // Populate the artifact items.
        List<Item> defaultItems = LinkedItemList.load(entries.getOrDefault(DEFAULT_ARTIFACT_ITEMS, new LinkedList<>()));
        List<Item> artifactItems = new ArrayList<>();

        // Artifact items defined in the definition.
        for (var itemEntry : definition.artifactItems()) {
            artifactItems.addAll(LinkedItemList.load(entries.getOrDefault(itemEntry, new LinkedList<>())));
        }

        if (artifactItems.isEmpty()) {
            if (defaultItems.isEmpty()) {
                throw new RuntimeException("No custom or default items, cannot continue");
            }
            log().debug(getClass(), "Using default artifact items list");
            artifactItems = defaultItems;
        }

        Collections.shuffle(artifactItems);
        var stack = new ItemStack(artifactItems.get(0));
        artifact = new ArtifactItem(stack);
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        var artifactTag = tag.getCompound(ARTIFACT_TAG);
        var item = new ArtifactItem();
        item.load(artifactTag);
        artifact = item;

        lootTables.clear();
        var list = tag.getList(LOOT_TABLES_TAG, 8);
        for (var t : list) {
            var lootTable = ResourceLocation.tryParse(t.getAsString());
            lootTables.add(lootTable);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        var artifactTag = new CompoundTag();
        artifact.save(artifactTag);
        tag.put(ARTIFACT_TAG, artifactTag);

        var list = new ListTag();
        for (var lootTable : lootTables) {
            var t1 = StringTag.valueOf(lootTable.toString());
            list.add(t1);
        }
        tag.put(LOOT_TABLES_TAG, list);
    }

    private ILog log() {
        return Mods.common(Strange.ID).log();
    }

    private LinkedList<Item> tryLoad(Map<ResourceLocation, LinkedList<ResourceLocation>> entries, ResourceLocation... keys) {
        for (var key : keys) {
            var list = LinkedItemList.load(entries.getOrDefault(key, new LinkedList<>()));
            if (!list.isEmpty()) {
                log().debug(getClass(), "Using list for key " + key);
                return list;
            }
        }
        return new LinkedList<>();
    }

    private ResourceLocation makeItemsKey(int villagerLevel, VillagerProfession villagerProfession) {
        return new ResourceLocation(Strange.ID, QuestHelper.getVillagerLevelName(villagerLevel)
            + "_" + QuestHelper.getVillagerProfessionName(villagerProfession)
            + "_artifact_items");
    }

    private boolean hasArtifact() {
        if (player == null) {
            return false;
        }

        return player.getInventory().items.stream().anyMatch(i -> {
            var tag = i.getTag();
            return tag != null && tag.getString(ArtifactItem.CUSTOM_TAG).equals(id());
        });
    }

    public class ArtifactItem implements Requirement {
        static final String ITEM_TAG = "item";
        static final String CUSTOM_TAG = "strange_artifact";

        public ItemStack item;

        private ArtifactItem() {}

        public ArtifactItem(ItemStack item) {
            var questId = id();
            addRandomEnchantment(item);
            item.getOrCreateTag().putString(CUSTOM_TAG, questId);

            // Add custom name
            addNamePrefix(item);

            // Add random colored glint
            var dyeColors = new ArrayList<>(Arrays.stream(DyeColor.values()).toList());
            ColoredGlints.applyColoredGlint(item, dyeColors.get(random().nextInt(dyeColors.size())));

            this.item = item;
        }

        @Override
        public boolean satisfied() {
            return remaining() == 0;
        }

        @Override
        public int total() {
            return 1;
        }

        @Override
        public int remaining() {
            if (player == null) {
                return total();
            }

            var remainder = total();

            if (remainder > 0) {
                // Make safe copy of the player's inventory.
                List<ItemStack> inventory = new ArrayList<>();
                for (var stack : player.getInventory().items) {
                    inventory.add(stack.copy());
                }

                for (var invItem : inventory) {
                    if (remainder <= 0) continue;

                    var tag = invItem.getTag();
                    if (tag == null) continue;

                    if (tag.contains(CUSTOM_TAG)) {
                        var questId = id();
                        var itemId = tag.getString(CUSTOM_TAG);
                        if (itemId.equals(questId)) {
                            remainder -= 1;
                        }
                    }
                }
            }

            return Math.max(0, remainder);
        }

        @Override
        public void complete() {
            if (player == null) {
                return;
            }

            var remainder = total();

            if (remainder > 0) {
                for (var invItem : player.getInventory().items) {
                    if (remainder <= 0) continue;

                    var tag = invItem.getTag();
                    if (tag == null) continue;

                    if (tag.contains(CUSTOM_TAG)) {
                        var questId = id();
                        var itemId = tag.getString(CUSTOM_TAG);
                        if (itemId.equals(questId)) {
                            var decrement = Math.min(remainder, invItem.getCount());
                            remainder -= decrement;
                            invItem.shrink(decrement);
                        }
                    }
                }
            }
        }

        @Override
        public void load(CompoundTag tag) {
            item = ItemStack.of(tag.getCompound(ITEM_TAG));
        }

        @Override
        public void save(CompoundTag tag) {
            var itemTag = new CompoundTag();
            item.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
        }

        protected void addRandomEnchantment(ItemStack stack) {
            var enchantments = BuiltInRegistries.ENCHANTMENT.entrySet().stream().map(Map.Entry::getValue).toList();
            var enchantment = enchantments.get(random().nextInt(enchantments.size()));
            Map<Enchantment, Integer> map = new HashMap<>();
            map.put(enchantment, enchantment.getMinLevel());
            EnchantmentHelper.setEnchantments(map, stack);
        }

        protected void addNamePrefix(ItemStack stack) {
            var prefixes = Arrays.stream(Component.translatable("gui.strange.descriptions.prefix").getString().split(",")).toList();

            var prefix = prefixes.get(random().nextInt(prefixes.size()));
            var name = stack.getItem().getName(stack);

            var newName = Component.translatable("gui.strange.descriptions.prefixed_name", prefix, name);
            stack.setHoverName(newName);
        }
    }
}
