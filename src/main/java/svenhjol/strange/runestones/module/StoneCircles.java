package svenhjol.strange.runestones.module;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.runestones.structure.StoneCirclePiece;
import svenhjol.strange.runestones.structure.StoneCircleStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, configureEnabled = false)
public class StoneCircles extends MesonModule
{
    public static final String NAME = "stone_circle";
    public static final String RESNAME = "strange:stone_circle";
    public static Structure<NoFeatureConfig> structure;

    @Config(name = "Add stone circle maps to loot", description = "If true, stone circle maps will be added to village chests.")
    public static boolean addMapsToLoot = true;

    @Config(name = "Allowed biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> biomesConfig = new ArrayList<>(Arrays.asList(
        BiomeHelper.getBiomeName(Biomes.PLAINS),
        BiomeHelper.getBiomeName(Biomes.MUSHROOM_FIELDS),
        BiomeHelper.getBiomeName(Biomes.SUNFLOWER_PLAINS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.WOODED_BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.DESERT),
        BiomeHelper.getBiomeName(Biomes.DESERT_LAKES),
        BiomeHelper.getBiomeName(Biomes.BEACH),
        BiomeHelper.getBiomeName(Biomes.RIVER),
        BiomeHelper.getBiomeName(Biomes.SAVANNA),
        BiomeHelper.getBiomeName(Biomes.SAVANNA_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.SNOWY_TUNDRA),
        BiomeHelper.getBiomeName(Biomes.SNOWY_BEACH),
        BiomeHelper.getBiomeName(Biomes.SWAMP),
        BiomeHelper.getBiomeName(Biomes.END_BARRENS),
        BiomeHelper.getBiomeName(Biomes.END_HIGHLANDS),
        BiomeHelper.getBiomeName(Biomes.END_MIDLANDS),
        BiomeHelper.getBiomeName(Biomes.NETHER)
    ));

    public static List<Biome> validBiomes = new ArrayList<>();

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && Strange.hasModule(Runestones.class);
    }

    @Override
    public void init()
    {
        structure = new StoneCircleStructure();

        RegistryHandler.registerStructure(structure, new ResourceLocation(Strange.MOD_ID, NAME));
        RegistryHandler.registerStructurePiece(StoneCirclePiece.PIECE, new ResourceLocation(Strange.MOD_ID, "scp"));

        biomesConfig.forEach(biomeName -> {
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

        validBiomes.forEach(biome -> {
            biome.addFeature(
                GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
                Biome.createDecoratedFeature(structure, IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, IFeatureConfig.NO_FEATURE_CONFIG);
        });
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        if (!addMapsToLoot) return;

        int weight = 0;
        int quality = 1;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_CARTOGRAPHER)) {
            weight = 40;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE)
        ) {
            weight = 12;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Items.MAP)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    BlockPos pos = context.get(LootParameters.POSITION);
                    if (pos != null) {
                        ServerWorld world = context.getWorld();
                        BlockPos structurePos = world.findNearestStructure(RESNAME, pos, 100, true);
                        if (structurePos != null) {
                            ItemStack map = FilledMapItem.setupNewMap(world, structurePos.getX(), structurePos.getZ(), (byte)2, true, true);
                            FilledMapItem.renderBiomePreviewMap(world, map);
                            MapData.addTargetDecoration(map, structurePos, "+", MapDecoration.Type.TARGET_X);
                            map.setDisplayName(new TranslationTextComponent("filled_map.stone_circle"));
                            return map;
                        }
                    }
                    return stack;
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }
}
