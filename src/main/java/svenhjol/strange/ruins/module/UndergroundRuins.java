package svenhjol.strange.ruins.module;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.StructureHelper.RegisterJigsawPieces;
import svenhjol.strange.ruins.structure.UndergroundConfig;
import svenhjol.strange.ruins.structure.UndergroundPiece;
import svenhjol.strange.ruins.structure.UndergroundStructure;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS)
public class UndergroundRuins extends MesonModule
{
    public static final String DIR = "underground";
    public static Structure<UndergroundConfig> structure;
    public static Map<Biome.Category, List<String>> ruins = new HashMap<>();
    public static Map<Biome.Category, Map<String, Integer>> sizes = new HashMap<>();
    public static Map<Biome.Category, List<ResourceLocation>> starts = new HashMap<>();
    public static List<Structure<?>> blacklist = new ArrayList<>(Arrays.asList(
        Feature.STRONGHOLD,
        Feature.OCEAN_MONUMENT,
        Feature.NETHER_BRIDGE,
        Feature.BURIED_TREASURE
    ));

    @Config(name = "Default size", description = "Controls how many pieces generate as part of a ruin.")
    public static int defaultSize = 2;

    @Config(name = "Additional pieces", description = "Random number of extra pieces that may be added to a ruin.")
    public static int variation = 1;

    @Override
    public void init()
    {
        structure = new UndergroundStructure(UndergroundConfig::deserialize);

        RegistryHandler.registerFeature(structure, new ResourceLocation("underground_ruin"));
        RegistryHandler.registerStructurePiece(UndergroundPiece.PIECE, new ResourceLocation(Strange.MOD_ID, "usp"));

        for (Biome biome : ForgeRegistries.BIOMES) {
            biome.addFeature(
                GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
                Biome.createDecoratedFeature(structure, new UndergroundConfig(1.0F), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, new UndergroundConfig(1.0F));
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        for (Biome.Category cat : Biome.Category.values()) {
            String catName = cat.getName().toLowerCase();
            String dir = DIR + "/" + catName;
            RegisterJigsawPieces register = new RegisterJigsawPieces(rm, dir);

            if (register.structures.size() == 0) continue;

            if (!ruins.containsKey(cat)) ruins.put(cat, new ArrayList<>());
            ruins.get(cat).addAll(register.structures);

            if (!starts.containsKey(cat)) starts.put(cat, new ArrayList<>());
            starts.get(cat).addAll(register.starts);

            if (!sizes.containsKey(cat)) sizes.put(cat, new HashMap<>());
            sizes.get(cat).putAll(register.sizes);
        }

        Meson.log(ruins);
    }
}
