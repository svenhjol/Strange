package svenhjol.strange.runestones.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.charm.tweaks.client.AmbientMusicClient;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.ruins.module.UndergroundRuins;
import svenhjol.strange.runestones.structure.StoneCircleConfig;
import svenhjol.strange.runestones.structure.StoneCircleStructure;
import svenhjol.strange.runestones.structure.VaultStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES)
public class StoneCircles extends MesonModule
{
    public static final ResourceLocation ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final String NAME = "stone_circle";
    public static Structure<StoneCircleConfig> structure;

    @Config(name = "Vault generation chance", description = "Chance (out of 1.0) of vaults generating beneath a stone circle.")
    public static double vaultChance = 0.66D;

    @Config(name = "Vault generation size", description = "Maximum number of rooms generated in any vault corridor.")
    public static int vaultSize = 6;

    @Config(name = "Outer stone circles only", description = "If true, vaults will only generate under stone circles in 'outer lands'.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Config(name = "Allowed biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> biomesConfig = new ArrayList<>(Arrays.asList(
        BiomeHelper.getBiomeName(Biomes.PLAINS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS),
        BiomeHelper.getBiomeName(Biomes.MUSHROOM_FIELDS),
        BiomeHelper.getBiomeName(Biomes.SUNFLOWER_PLAINS),
        BiomeHelper.getBiomeName(Biomes.DESERT),
        BiomeHelper.getBiomeName(Biomes.DESERT_LAKES),
        BiomeHelper.getBiomeName(Biomes.BEACH),
        BiomeHelper.getBiomeName(Biomes.SAVANNA),
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
    public void init()
    {
        structure = new StoneCircleStructure(StoneCircleConfig::deserialize);

        RegistryHandler.registerFeature(structure, new ResourceLocation("stone_circle"));
        RegistryHandler.registerStructurePiece(StoneCircleStructure.STONE_CIRCLE_PIECE, new ResourceLocation(Strange.MOD_ID, "scp"));
        RegistryHandler.registerStructurePiece(VaultStructure.VAULT_PIECE, new ResourceLocation(Strange.MOD_ID, "vp"));

        biomesConfig.forEach(biomeName -> {
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

        Registry.BIOME.forEach(biome -> {
            biome.addFeature(
                GenerationStage.Decoration.SURFACE_STRUCTURES,
                Biome.createDecoratedFeature(structure, new StoneCircleConfig(1.0F), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
        });

        validBiomes.forEach(biome -> {
            biome.addStructure(structure, new StoneCircleConfig(1.0F));
        });

        UndergroundRuins.blacklist.add(structure);
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_THARNA, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            return Outerlands.isOuterPos(player.getPosition()) && player.world.rand.nextFloat() < 0.25F;
        });
    }
}
