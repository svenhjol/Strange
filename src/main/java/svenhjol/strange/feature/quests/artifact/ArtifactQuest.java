package svenhjol.strange.feature.quests.artifact;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.feature.colored_glints.ColoredGlints;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Requirement;

import java.util.*;

public class ArtifactQuest extends Quest {
    static final String ARTIFACT_TAG = "artifact";
    static final String LOOT_TABLES_TAG = "loot_tables";

    ArtifactItem artifact;
    List<ResourceLocation> lootTables = new ArrayList<>();

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

        if (!artifact.discovered && inProgress() && !lootTables.isEmpty()) {
            for (var lootTable : lootTables) {
                if (lootTable.equals(lootTableId)) {
                    if (random.nextDouble() < artifact.chance) {
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
    protected void makeRequirements(QuestDefinition definition) {
        var artifact = definition.artifact().take(random());

        this.lootTables = artifact.lootTables();
        this.artifact = new ArtifactItem(artifact.item(), artifact.chance());
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

    public class ArtifactItem implements Requirement {
        static final String ITEM_TAG = "item";
        static final String CUSTOM_TAG = "strange_artifact";
        static final String CHANCE_TAG = "chance";
        static final String DISCOVERED_TAG = "discovered";

        public ItemStack item;
        public double chance;
        public boolean discovered;

        private ArtifactItem() {}

        public ArtifactItem(ItemStack item, double chance) {
            var questId = id();
            addRandomEnchantment(item);
            item.getOrCreateTag().putString(CUSTOM_TAG, questId);

            // Add custom name
            addNamePrefix(item);

            // Add random colored glint
            var dyeColors = new ArrayList<>(Arrays.stream(DyeColor.values()).toList());
            ColoredGlints.applyColoredGlint(item, dyeColors.get(random().nextInt(dyeColors.size())));

            this.item = item;
            this.chance = chance;
            this.discovered = false;
        }

        @Override
        public boolean satisfied() {
            var satisfied = remaining() == 0;
            if (satisfied && !discovered) {
                discovered = true;
            }
            return satisfied;
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
            chance = tag.getDouble(CHANCE_TAG);
            discovered = tag.getBoolean(DISCOVERED_TAG);
        }

        @Override
        public void save(CompoundTag tag) {
            var itemTag = new CompoundTag();
            item.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
            tag.putDouble(CHANCE_TAG, chance);
            tag.putBoolean(DISCOVERED_TAG, discovered);
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
