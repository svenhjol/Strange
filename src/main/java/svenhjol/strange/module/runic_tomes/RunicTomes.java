package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runic_tomes.loot.BiomeTomeLootFunction;
import svenhjol.strange.module.runic_tomes.loot.DimensionTomeLootFunction;
import svenhjol.strange.module.runic_tomes.loot.StructureTomeLootFunction;

import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class RunicTomes extends CharmModule {
    public static final ResourceLocation RUNIC_LECTERN_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "runic_lectern");
    public static final ResourceLocation MSG_CLIENT_SET_LECTERN_TOME = new ResourceLocation(Strange.MOD_ID, "client_set_lectern_tome");

    public static RunicLecternBlock RUNIC_LECTERN;
    public static MenuType<RunicLecternMenu> RUNIC_LECTERN_MENU;
    public static BlockEntityType<RunicLecternBlockEntity> RUNIC_LECTERN_BLOCK_ENTITY;
    public static RunicTomeItem RUNIC_TOME;
    public static LootItemFunctionType BIOME_TOME_LOOT;
    public static LootItemFunctionType DIMENSION_TOME_LOOT;
    public static LootItemFunctionType STRUCTURE_TOME_LOOT;

    private static final List<ResourceLocation> VALID_LOOT_FOR_BIOMES;
    private static final List<ResourceLocation> VALID_LOOT_FOR_DIMENSIONS;
    private static final List<ResourceLocation> VALID_LOOT_FOR_STRUCTURES;

    @Override
    public void register() {
        RUNIC_TOME = new RunicTomeItem(this);
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
