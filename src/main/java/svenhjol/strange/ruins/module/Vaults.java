package svenhjol.strange.ruins.module;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.StructureHelper;
import svenhjol.strange.base.helper.VersionHelper;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.ruins.structure.VaultPiece;
import svenhjol.strange.ruins.structure.VaultStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS, hasSubscriptions = true,
    description = "Large underground complexes with rare treasure.")
public class Vaults extends MesonModule {
    public static final String NAME = "vaults";
    public static final String RESNAME = "strange:vaults";
    public static final String VAULTS_DIR = "vaults";
    public static final String VAULTS_LOCAL = "vaults_local";
    public static Structure<NoFeatureConfig> structure;

    @Config(name = "Vault size", description = "Vaults size. This controls how many corridors and rooms will spawn.")
    public static int size = 8;

    @Config(name = "Outerlands only", description = "If true, vaults will only generate in the Outerlands.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Config(name = "Generate below Y value", description = "Vaults will try and generate below this Y value.")
    public static int generateBelow = 68;

    @Config(name = "Generate above Y value", description = "Vaults will try and generate above this Y value.")
    public static int generateAbove = 24;

    public static final List<Biome> validBiomes = new ArrayList<>();

    @Override
    public void init() {
        structure = new VaultStructure();

        RegistryHandler.registerStructure(structure, new ResourceLocation(Strange.MOD_ID, NAME));
        RegistryHandler.registerStructurePiece(VaultPiece.PIECE, new ResourceLocation(Strange.MOD_ID, "vp"));

        final List<Biome> overworldBiomes = StructureHelper.getOverworldBiomes();

        ForgeRegistries.BIOMES.forEach(biome -> {
            if (!overworldBiomes.contains(biome))
                return;

            VersionHelper.addStructureToBiome(structure, biome);
        });

        validBiomes.addAll(Arrays.asList(
            Biomes.MOUNTAINS, Biomes.MOUNTAIN_EDGE
        ));
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        final IReloadableResourceManager rm = event.getServer().getResourceManager();
        new StructureHelper.RegisterJigsawPieces(rm, VAULTS_DIR); // for normal vault pieces
        new StructureHelper.RegisterJigsawPieces(rm, VAULTS_LOCAL); // these are pieces for vaults that are part of the innerlands
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        int weight = 0;
        int quality = 1;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_PILLAGER_OUTPOST)) {
            weight = 40;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_CARTOGRAPHER)) {
            weight = 40;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Items.MAP)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    BlockPos pos = context.get(LootParameters.POSITION);
                    if (pos != null && isValidPosition(pos)) {
                        ServerWorld world = context.getWorld();
                        BlockPos structurePos = world.findNearestStructure(RESNAME, pos, 200, true);
                        if (structurePos != null) {
                            ItemStack map = FilledMapItem.setupNewMap(world, structurePos.getX(), structurePos.getZ(), (byte) 2, true, true);
                            FilledMapItem.renderBiomePreviewMap(world, map);
                            MapData.addTargetDecoration(map, structurePos, "+", MapDecoration.Type.RED_X);
                            map.setDisplayName(new TranslationTextComponent("filled_map.vaults"));
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

    public static boolean isValidPosition(BlockPos pos) {
        if (!Meson.isModuleEnabled("strange:outerlands") || !Vaults.outerOnly) return true;
        return Meson.isModuleEnabled("strange:outerlands") && Outerlands.isOuterPos(pos);
    }
}
