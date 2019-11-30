package svenhjol.strange.undergroundruins.module;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.undergroundruins.structure.UndergroundRuinConfig;
import svenhjol.strange.undergroundruins.structure.UndergroundRuinStructure;

import java.util.ArrayList;
import java.util.Collection;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.UNDERGROUND_RUINS)
public class UndergroundRuins extends MesonModule
{
    public static Structure<UndergroundRuinConfig> structure;

    @Override
    public void init()
    {
        structure = new UndergroundRuinStructure(UndergroundRuinConfig::deserialize);

        RegistryHandler.registerFeature(structure, new ResourceLocation("underground_ruin"));
        RegistryHandler.registerStructurePiece(UndergroundRuinStructure.UNDERGROUND_RUIN_PIECE, new ResourceLocation(Strange.MOD_ID, "usp"));

        for (Biome biome : ForgeRegistries.BIOMES) {
            biome.addFeature(
                GenerationStage.Decoration.SURFACE_STRUCTURES,
                Biome.createDecoratedFeature(structure, new UndergroundRuinConfig(1.0F), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, new UndergroundRuinConfig(1.0F));
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        try {
            for (Biome.Category cat : Biome.Category.values()) {
                String catName = cat.getName().toLowerCase();
                Collection<ResourceLocation> resources = rm.getAllResourceLocations("structures/underground_ruins/" + catName, file -> file.endsWith(".nbt"));

                for (ResourceLocation res : resources) {
                    String name = res.getPath()
                        .replace(".nbt", "")
                        .replace("structures/", "");

                    if (!UndergroundRuinStructure.biomeRuins.containsKey(cat)) {
                        UndergroundRuinStructure.biomeRuins.put(cat, new ArrayList<>());
                    }
                    UndergroundRuinStructure.biomeRuins.get(cat).add(new ResourceLocation(Strange.MOD_ID, name));
                }
            }
        } catch (Exception e) {
            Meson.warn("Could not load structures for biome category", e);
        }

        Meson.log(UndergroundRuinStructure.biomeRuins);
    }
}
