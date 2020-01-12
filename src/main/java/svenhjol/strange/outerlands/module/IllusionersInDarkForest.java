package svenhjol.strange.outerlands.module;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;

import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.OUTERLANDS, hasSubscriptions = true,
    description = "Illusioners may spawn in Dark Forests in the Outerlands.\n" +
        "If Outerlands is disabled, Illusioners may spawn anywhere.")
public class IllusionersInDarkForest extends MesonModule
{
    @Config(name = "Spawn weight", description = "The higher this value, the more Illusioners will spawn. For comparison, zombie weight is 100.")
    public static int weight = 50;

    /**
     * Register illusioner as a mob in dark forest biome and variants
     */
    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        Biome.SpawnListEntry illusioner = new Biome.SpawnListEntry(EntityType.ILLUSIONER, weight, 1, 1);
        List<Biome> validBiomes = Arrays.asList(Biomes.DARK_FOREST, Biomes.DARK_FOREST_HILLS);

        for (Biome biome : validBiomes) {
            biome.getSpawns(EntityClassification.MONSTER).add(illusioner);
        }
    }

    /**
     * Don't allow spawns underground (beneath sea level).
     */
    @SubscribeEvent
    public void onSpawn(LivingSpawnEvent.CheckSpawn event)
    {
        if (!event.isSpawner()
            && event.getEntityLiving() instanceof IllusionerEntity
            && event.getResult() != Event.Result.DENY
            && event.getEntityLiving().world instanceof ServerWorld
        ) {
            BlockPos pos = event.getEntityLiving().getPosition();

            if (Strange.hasModule(Outerlands.class) && !Outerlands.isOuterPos(pos)) {
                Meson.debug("Not spawning Illusioner, not in Outerlands");
                event.setResult(Event.Result.DENY);
            } else if (pos.getY() < event.getEntityLiving().world.getSeaLevel() - 5) {
                Meson.debug("Not spawning Illusioner, pos is below sea level");
                event.setResult(Event.Result.DENY);
            } else {
                Meson.debug("Spawning Illusioner at " + pos);
            }
        }
    }
}
