package svenhjol.strange.totems.module;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfEnchantingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfEnchanting extends MesonModule
{
    public static TotemOfEnchantingItem item;

    @Override
    public void init()
    {
        item = new TotemOfEnchantingItem(this);
    }

    @SubscribeEvent
    public void onPlayerPickupXp(PlayerPickupXpEvent event)
    {
        if (event.getPlayer().getHeldItemMainhand().getItem() == item
            || event.getPlayer().getHeldItemOffhand().getItem() == item) {
            PlayerEntity player = event.getPlayer();
            ExperienceOrbEntity orb = event.getOrb();
            for (Hand hand : Hand.values()) {
                if (player.getHeldItem(hand).getItem() == item) {
                    TotemOfEnchantingItem.addXp(player.getHeldItem(hand), orb.getXpValue());
                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.25F);
                    break;
                }
            }
            orb.remove();
            event.setCanceled(true);
        }
    }
}
