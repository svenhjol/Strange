package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.runic_tomes.loot.BiomeTomeLootFunction;
import svenhjol.strange.module.runic_tomes.loot.DimensionTomeLootFunction;
import svenhjol.strange.module.runic_tomes.loot.StructureTomeLootFunction;
import svenhjol.strange.module.runic_tomes.network.ServerSendSetTome;

import java.util.*;
import java.util.function.BiFunction;

@CommonModule(mod = Strange.MOD_ID, description = "Runic Tomes can be created on a writing desk and provide fast transport between locations.\n" +
    "They can also be found inside stronghold libraries.")
public class RunicTomes extends CharmModule {
    public static final ResourceLocation RUNIC_LECTERN_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runic_lectern");

    public static ServerSendSetTome SERVER_SEND_SET_TOME;

    public static RunicLecternBlock RUNIC_LECTERN;
    public static MenuType<RunicLecternMenu> RUNIC_LECTERN_MENU;
    public static BlockEntityType<RunicLecternBlockEntity> RUNIC_LECTERN_BLOCK_ENTITY;
    public static LootItemFunctionType BIOME_TOME_LOOT;
    public static LootItemFunctionType DIMENSION_TOME_LOOT;
    public static LootItemFunctionType STRUCTURE_TOME_LOOT;

    public static Map<String, RunicTomeItem> RUNIC_TOMES = new HashMap<>();
    public static List<String> VALID_BRANCHES;

    public static final List<BiFunction<Level, Random, ResourceLocation>> DIMENSION_TOME_LOOT_CALLBACKS = new ArrayList<>();
    public static final List<ResourceLocation> VALID_LOOT_FOR_BIOMES;
    public static final List<ResourceLocation> VALID_LOOT_FOR_DIMENSIONS;
    public static final List<ResourceLocation> VALID_LOOT_FOR_STRUCTURES;

    private static final ResourceLocation TRIGGER_ACTIVATE_TOME = new ResourceLocation(Strange.MOD_ID, "activate_tome");

    @Override
    public void register() {
        for (String branch : VALID_BRANCHES) {
            RUNIC_TOMES.put(branch, new RunicTomeItem(this, branch));
        }

        RUNIC_LECTERN = new RunicLecternBlock(this);
        RUNIC_LECTERN_MENU = CommonRegistry.menu(RUNIC_LECTERN_BLOCK_ID, RunicLecternMenu::new);
        RUNIC_LECTERN_BLOCK_ENTITY = CommonRegistry.blockEntity(RUNIC_LECTERN_BLOCK_ID, RunicLecternBlockEntity::new, RUNIC_LECTERN);
        BIOME_TOME_LOOT = CommonRegistry.lootFunctionType(new ResourceLocation(Strange.MOD_ID, "biome_tome_loot"), new LootItemFunctionType(new BiomeTomeLootFunction.Serializer()));
        DIMENSION_TOME_LOOT = CommonRegistry.lootFunctionType(new ResourceLocation(Strange.MOD_ID, "dimension_tome_loot"), new LootItemFunctionType(new DimensionTomeLootFunction.Serializer()));
        STRUCTURE_TOME_LOOT = CommonRegistry.lootFunctionType(new ResourceLocation(Strange.MOD_ID, "structure_tome_loot"), new LootItemFunctionType(new StructureTomeLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        UseBlockCallback.EVENT.register(this::handleUseBlock);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        SERVER_SEND_SET_TOME = new ServerSendSetTome();

        // Add a loot callback for tomes in the Nether and End.
        DIMENSION_TOME_LOOT_CALLBACKS.add((level, random) -> {
            if (DimensionHelper.isNether(level) && random.nextFloat() < 0.66F) {
                return DimensionHelper.getDimension(Level.NETHER);
            }
            return null;
        });
        DIMENSION_TOME_LOOT_CALLBACKS.add((level, random) -> {
            if (DimensionHelper.isEnd(level) && random.nextFloat() < 0.66F) {
                return DimensionHelper.getDimension(random.nextBoolean() ? Level.END : Level.NETHER);
            }
            return null;
        });
    }

    public static void triggerActivateTome(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_ACTIVATE_TOME);
    }

    /**
     * We need to be able to handle converting a vanilla lectern into a "runic" lectern.
     * This custom version of the lectern holds a runic tome and the sacrifice stack used to activate it.
     * When a tome is removed, the runic lectern should turn back into a vanilla one.
     */
    private InteractionResult handleUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.PASS;

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(hitPos);
        Block block = state.getBlock();

        if (block instanceof LecternBlock
            && level.getBlockEntity(hitPos) instanceof LecternBlockEntity lectern
            && !lectern.hasBook()
            && player.getItemInHand(hand).getItem() instanceof RunicTomeItem
        ) {
            Direction facing = state.getValue(LecternBlock.FACING);
            BlockState newState = RUNIC_LECTERN.defaultBlockState();
            newState = newState.setValue(RunicLecternBlock.FACING, facing);
            level.setBlock(hitPos, newState, 2);
            if (level.getBlockEntity(hitPos) instanceof RunicLecternBlockEntity runicLectern) {
                ItemStack held = player.getItemInHand(hand);
                ItemStack tome = held.copy();
                held.shrink(1);
                runicLectern.setTome(tome);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter lootTableSetter) {
        if (VALID_LOOT_FOR_BIOMES.contains(id)) {
            buildLootPool(UniformGenerator.between(1, 2), new BiomeTomeLootFunction(new LootItemCondition[0]), supplier);
        }
        if (VALID_LOOT_FOR_DIMENSIONS.contains(id)) {
            buildLootPool(ConstantValue.exactly(1), new DimensionTomeLootFunction(new LootItemCondition[0]), supplier);
        }
        if (VALID_LOOT_FOR_STRUCTURES.contains(id)) {
            buildLootPool(UniformGenerator.between(1, 2), new StructureTomeLootFunction(new LootItemCondition[0]), supplier);
        }
    }

    private void buildLootPool(NumberProvider rolls, LootItemConditionalFunction lootFunction, FabricLootSupplierBuilder supplier) {
        FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
            .rolls(rolls)
            .with(LootItem.lootTableItem(Items.BOOK)
                .setWeight(1)
                .apply(() -> lootFunction));

        supplier.withPool(builder);
    }

    static {
        VALID_BRANCHES = Arrays.asList(
            BiomeBranch.NAME,
            BookmarkBranch.NAME,
            DimensionBranch.NAME,
            DiscoveryBranch.NAME,
            StructureBranch.NAME
        );

        VALID_LOOT_FOR_DIMENSIONS = List.of(
            BuiltInLootTables.STRONGHOLD_LIBRARY
        );
        VALID_LOOT_FOR_BIOMES = List.of(
            BuiltInLootTables.STRONGHOLD_LIBRARY
        );
        VALID_LOOT_FOR_STRUCTURES = List.of(
            BuiltInLootTables.STRONGHOLD_LIBRARY
        );
    }
}
