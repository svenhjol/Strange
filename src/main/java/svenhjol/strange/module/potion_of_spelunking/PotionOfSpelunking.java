package svenhjol.strange.module.potion_of_spelunking;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.event.PlayerTickCallback;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.potion_of_spelunking.network.ServerSendShowParticles;

import java.util.*;

@CommonModule(mod = Strange.MOD_ID)
public class PotionOfSpelunking extends CharmModule {
    private static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;

    public static ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "potion_of_spelunking_loot");
    public static LootItemFunctionType LOOT_FUNCTION;
    public static List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();

    public static SpelunkingEffect SPELUNKING_EFFECT;
    public static SpelunkingPotion SPELUNKING_POTION;

    public static ServerSendShowParticles SERVER_SEND_SHOW_PARTICLES;
    public static final ResourceLocation TRIGGER_TRAVEL_TO_SPAWN_POINT = new ResourceLocation(Strange.MOD_ID, "has_spelunking_effect");

    public static final Map<Block, DyeColor> BLOCKS = new HashMap<>();
    public static final Map<Tag<Block>, DyeColor> BLOCK_TAGS = new HashMap<>();

    @Config(name = "Duration", description = "Duration (in seconds) of the spelunking effect.")
    public static int duration = 30;

    @Config(name = "Depth", description = "Depth (in blocks) below the player in which blocks will be revealed.")
    public static int depth = 32;

    @Config(name = "Revealed blocks", description = "Block or tag IDs (and colors) that are revealed with the spelunking effect.")
    public static List<String> configBlocks = Arrays.asList(
        "#minecraft:coal_ores -> black",
        "#minecraft:iron_ores -> light_gray",
        "#minecraft:redstone_ores -> red",
        "#minecraft:gold_ores -> yellow",
        "#minecraft:copper_ores -> orange",
        "#minecraft:lapis_ores -> blue",
        "#minecraft:diamond_ores -> cyan",
        "#minecraft:emerald_ores -> lime",
        "minecraft:ancient_debris -> brown",
        "minecraft:nether_quartz_ore -> light_gray",
        "minecraft:amethyst_block -> purple"
    );

    @Override
    public void register() {
        SPELUNKING_EFFECT = new SpelunkingEffect(this);
        SPELUNKING_POTION = new SpelunkingPotion(this);
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new PotionOfSpelunkingLootFunction.Serializer()));

        configBlocks.forEach(def -> {
            String blockId;
            DyeColor color;
            String[] split = def.split("->");

            if (split.length == 2) {
                blockId = split[0].trim();
                color = DyeColor.byName(split[1].trim(), DEFAULT_COLOR);
            } else if (split.length == 1) {
                blockId = split[0].trim();
                color = DEFAULT_COLOR;
            } else {
                return;
            }

            if (blockId.startsWith("#")) {
                // it's a tag
                ResourceLocation id = new ResourceLocation(blockId.substring(1));
                BLOCK_TAGS.put(TagFactory.BLOCK.create(id), color);
            } else {
                // it's a block
                ResourceLocation id = new ResourceLocation(blockId);
                Registry.BLOCK.getOptional(id).ifPresent(block -> BLOCKS.put(block, color));
            }
        });
    }

    @Override
    public void runWhenEnabled() {
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        SERVER_SEND_SHOW_PARTICLES = new ServerSendShowParticles();
        VALID_LOOT_TABLES.add(BuiltInLootTables.SIMPLE_DUNGEON);
    }

    private void handlePlayerTick(Player player) {
        if (!player.level.isClientSide
            && player.level.getGameTime() % 15 == 0
            && player.hasEffect(SPELUNKING_EFFECT)
            && !BLOCKS.isEmpty()
        ) {
            ServerLevel level = (ServerLevel)player.level;
            BlockPos playerPos = player.blockPosition();
            Map<BlockPos, DyeColor> found = new WeakHashMap<>();

            BLOCK_TAGS.forEach((tag, color) -> {
                Optional<BlockPos> closest = BlockPos.findClosestMatch(playerPos.below((depth/2) - 2), 8, depth / 2,
                    pos -> level.getBlockState(pos).is(tag));
                closest.ifPresent(blockPos -> found.put(blockPos, color));
            });

            BLOCKS.forEach((block, color) -> {
                Optional<BlockPos> closest = BlockPos.findClosestMatch(playerPos.below(depth/2), 8, depth / 2,
                    pos -> block.equals(level.getBlockState(pos).getBlock()));
                closest.ifPresent(blockPos -> found.put(blockPos, color));
            });

            if (found.isEmpty()) return;

            SERVER_SEND_SHOW_PARTICLES.send((ServerPlayer) player, found);
            CharmAdvancements.ACTION_PERFORMED.trigger((ServerPlayer) player, TRIGGER_TRAVEL_TO_SPAWN_POINT);
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter lootTableSetter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new PotionOfSpelunkingLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
