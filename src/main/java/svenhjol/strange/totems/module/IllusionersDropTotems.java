package svenhjol.strange.totems.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.outerlands.module.Outerlands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true,
    description = "An Illusioner drops a Totem when killed by a player.")
public class IllusionersDropTotems extends MesonModule
{
    @Config(name = "Drop chance",
        description = "Chance (out of 1.0) of an Illusioner dropping a totem when killed by the player."
    )
    public static double chance = 1.0D;

    @Config(name = "Only drop when in Outerlands",
        description = "If true, Illusioners only drop totems when in the Outerlands.")
    public static boolean outerlands = true;

    @Config(name = "Dropped totems",
        description = "List of totems that may be dropped by an Illusioner." +
            "If these totems are disabled, the Illusioner will drop a Totem of Undying."
    )
    public static List<String> totemsConfig = new ArrayList<>(Arrays.asList(
        "strange:totem_of_returning",
        "strange:totem_of_shielding"
    ));

    public static List<Item> totems = new ArrayList<>();

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        totemsConfig.forEach(name -> {
            Optional<Item> itemValue = Registry.ITEM.getValue(new ResourceLocation(name));
            if (!itemValue.isPresent()) return;
            totems.add(itemValue.get());
        });
    }

    @SubscribeEvent
    public void onIllusionerDrops(LivingDropsEvent event)
    {
        if (!event.getEntityLiving().world.isRemote
            && event.getEntityLiving() instanceof IllusionerEntity
            && (double)event.getEntityLiving().world.rand.nextFloat() <= chance
        ) {
            Item totemItem;
            Entity entity = event.getEntity();
            World world = entity.getEntityWorld();

            if (outerlands && (Strange.loader.hasModule(Outerlands.class) && !Outerlands.isOuterPos(entity.getPosition())))
                return;

            if (!totems.isEmpty()) {
                totemItem = totems.get(world.rand.nextInt(totems.size()));
            } else {
                totemItem = Items.TOTEM_OF_UNDYING;
            }

            if (totemItem == null) return;
            ItemStack totem = new ItemStack(totemItem);
            event.getDrops().add(new ItemEntity(world, entity.posX, entity.posY, entity.posZ, totem));
        }
    }
}
