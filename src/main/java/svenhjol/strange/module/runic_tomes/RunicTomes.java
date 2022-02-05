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
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.runic_tomes.loot.RunicTomeLootFunction;
import svenhjol.strange.module.runic_tomes.network.ServerSendSetTome;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID, description = "Runic Tomes can be created on a writing desk and provide fast transport between locations.\n" +
    "They can also be found inside stronghold libraries.")
public class RunicTomes extends CharmModule {
    public static final ResourceLocation RUNIC_LECTERN_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runic_lectern");

    public static ServerSendSetTome SERVER_SEND_SET_TOME;

    public static RunicLecternBlock RUNIC_LECTERN;
    public static MenuType<RunicLecternMenu> RUNIC_LECTERN_MENU;
    public static BlockEntityType<RunicLecternBlockEntity> RUNIC_LECTERN_BLOCK_ENTITY;
    public static LootItemFunctionType RUNIC_TOME_LOOT;

    public static Map<String, RunicTomeItem> RUNIC_TOMES = new HashMap<>();
    public static List<String> VALID_BRANCHES;
    public static Map<String, Float> interestingDestinations = new HashMap<>();
    public static Map<ResourceLocation, List<ResourceLocation>> dimensionTomes = new HashMap<>();

    public static final List<ResourceLocation> VALID_LOOT_TABLES;
    private static final ResourceLocation TRIGGER_ACTIVATE_TOME = new ResourceLocation(Strange.MOD_ID, "activate_tome");

    @Override
    public void register() {
        for (String branch : VALID_BRANCHES) {
            RUNIC_TOMES.put(branch, new RunicTomeItem(this, branch));
        }

        RUNIC_LECTERN = new RunicLecternBlock(this);
        RUNIC_LECTERN_MENU = CommonRegistry.menu(RUNIC_LECTERN_BLOCK_ID, RunicLecternMenu::new);
        RUNIC_LECTERN_BLOCK_ENTITY = CommonRegistry.blockEntity(RUNIC_LECTERN_BLOCK_ID, RunicLecternBlockEntity::new, RUNIC_LECTERN);
        RUNIC_TOME_LOOT = CommonRegistry.lootFunctionType(new ResourceLocation(Strange.MOD_ID, "runic_tome_loot"), new LootItemFunctionType(new RunicTomeLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        UseBlockCallback.EVENT.register(this::handleUseBlock);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        SERVER_SEND_SET_TOME = new ServerSendSetTome();

        addDimensionTomes(Level.OVERWORLD.location(), Arrays.asList(Level.OVERWORLD.location(), Level.NETHER.location()));
        addDimensionTomes(Level.NETHER.location(), Arrays.asList(Level.OVERWORLD.location(), Level.NETHER.location()));
        addDimensionTomes(Level.END.location(), Arrays.asList(Level.OVERWORLD.location(), Level.NETHER.location(), Level.END.location()));

        addInterestingDestination("minecraft:stronghold", 0.8F);
    }

    public static void addInterestingDestination(String location, float difficulty) {
        interestingDestinations.put(location, difficulty);
    }

    public static void addDimensionTomes(ResourceLocation dimension, List<ResourceLocation> tomes) {
        dimensionTomes.put(dimension, tomes);
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
        if (VALID_LOOT_TABLES.contains(id)) {
            buildLootPool(UniformGenerator.between(0, 3), new RunicTomeLootFunction(new LootItemCondition[0]), supplier);
        }
    }

    private void buildLootPool(NumberProvider rolls, LootItemConditionalFunction lootFunction, FabricLootSupplierBuilder supplier) {
        var builder = FabricLootPoolBuilder.builder()
            .rolls(rolls)
            .with(LootItem.lootTableItem(Items.BOOK)
                .setWeight(1)
                .apply(() -> lootFunction));

        supplier.withPool(builder);
    }

    static {
        VALID_BRANCHES = Arrays.asList(
            BookmarkBranch.NAME,
            DimensionBranch.NAME,
            DiscoveryBranch.NAME
        );

        VALID_LOOT_TABLES = List.of(
            BuiltInLootTables.STRONGHOLD_LIBRARY
        );
    }
}
