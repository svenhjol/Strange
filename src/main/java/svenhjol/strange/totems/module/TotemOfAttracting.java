package svenhjol.strange.totems.module;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfAttractingItem;

import java.util.List;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfAttracting extends MesonModule
{
    public static TotemOfAttractingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 100;

    @Config(name = "Attraction range", description = "Drops within this range of the player will be automatically picked up.")
    public static int range = 10;

    @Override
    public void init()
    {
        item = new TotemOfAttractingItem(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START
            && event.side.isServer()
            && event.player.world.getGameTime() % 5 == 0
            && event.player.getHeldItemOffhand().getItem() == item
        ) {
            int r = range;
            double x = event.player.posX;
            double y = event.player.posY;
            double z = event.player.posZ;

            List<ItemEntity> items = event.player.world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(
                x - r, y - r, z - r, x + r, y + r, z + r));

            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if (item.getItem().isEmpty() || item.removed) continue;
                    item.setPosition(x, y, z);
                }

                if (event.player.world.rand.nextInt(20) == 0) {
                    ItemStack held = event.player.getHeldItemOffhand();
                    boolean dead = held.attemptDamageItem(1, event.player.world.rand, (ServerPlayerEntity)event.player);

                    if (dead) {
                        held.shrink(1);
                        event.player.world.playSound(null, event.player.posX, event.player.posY, event.player.posZ, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    }
                }
            }
        }
    }
}
