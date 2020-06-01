package svenhjol.strange.stonecircles.module;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.StructureHelper;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.stonecircles.structure.VaultPiece;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS, hasSubscriptions = true,
    description = "Large underground complexes with rare treasure.")
public class Vaults extends MesonModule {
    public static final String VAULTS_DIR = "vaults";
    public static final String VAULTS_LOCAL = "vaults_local";
    public static Structure<NoFeatureConfig> structure;

    @Config(name = "Vault size", description = "Vaults size. This controls how many corridors and rooms will spawn.")
    public static int size = 8;

    @Config(name = "Vault chance", description = "Chance of vaults generating below a stone circle.")
    public static double vaultChance = 0.33D;

    @Config(name = "Outerlands only", description = "If true, vaults will only generate in the Outerlands.\n" +
        "This has no effect if the Outerlands module is disabled.")
    public static boolean outerOnly = true;

    @Config(name = "Generate below Y value", description = "Vaults will try and generate below this Y value.")
    public static int generateBelow = 62;

    @Config(name = "Generate above Y value", description = "Vaults will try and generate above this Y value.")
    public static int generateAbove = 5;

    @Override
    public void init() {
        RegistryHandler.registerStructurePiece(VaultPiece.PIECE, new ResourceLocation(Strange.MOD_ID, "vp"));
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        final IReloadableResourceManager rm = event.getServer().getResourceManager();
        new StructureHelper.RegisterJigsawPieces(rm, VAULTS_DIR); // for normal vault pieces
        new StructureHelper.RegisterJigsawPieces(rm, VAULTS_LOCAL); // these are pieces for vaults that are part of the innerlands
    }

    public static boolean isValidPosition(BlockPos pos) {
        if (!Meson.isModuleEnabled("strange:outerlands") || !Vaults.outerOnly) return true;
        return Meson.isModuleEnabled("strange:outerlands") && Outerlands.isOuterPos(pos);
    }
}
