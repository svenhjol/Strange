package svenhjol.strange.totems.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true,
    description = "An Illusioner drops a Totem when killed by a player.")
public class IllusionersDropTotems extends MesonModule
{
    @Config(name = "Drop chance",
        description = "Chance (out of 1.0) of an Illusioner dropping a Totem when killed by the player."
    )
    public static double chance = 1.0D;

    @Config(name = "Dropped totems",
        description = "List of totems that may be dropped by an Illusioner."
    )
    public static List<String> totemsConfig = new ArrayList<>(Arrays.asList(
        "strange:totem_of_returning",
        "strange:totem_of_shielding"
    ));

    public static List<Item> totems = new ArrayList<>();

    @Override
    public void init()
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
            && event.getSource().getTrueSource() instanceof PlayerEntity
            && (double)event.getEntityLiving().world.rand.nextFloat() <= chance
        ) {
            if (totems.isEmpty()) return;

            Entity entity = event.getEntity();
            World world = entity.getEntityWorld();
            Item totemItem = totems.get(world.rand.nextInt(totems.size()));
            if (totemItem == null) return;
            ItemStack totem = new ItemStack(totemItem);
            event.getDrops().add(new ItemEntity(world, entity.posX, entity.posY, entity.posZ, totem));
        }
    }
}
