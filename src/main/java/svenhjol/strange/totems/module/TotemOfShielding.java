package svenhjol.strange.totems.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeHelper;
import svenhjol.strange.totems.item.TotemOfShieldingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfShielding extends MesonModule
{
    public static TotemOfShieldingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 120;

    @Config(name = "Damage multiplier", description = "Player damage is multiplied by this amount before being transferred to the Totem." )
    public static double damageMultiplier = 0.5D;

    @Override
    public void init()
    {
        item = new TotemOfShieldingItem(this);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity
            && !event.isCanceled()
        ) {
            PlayerEntity player = (PlayerEntity)event.getEntityLiving();

            for (Hand hand : Hand.values()) {
                ItemStack held = player.getHeldItem(hand);

                if (held.getItem() instanceof TotemOfShieldingItem) {
                    double damageAmount = event.getAmount() * damageMultiplier;
                    int damage = StrangeHelper.damageTotem(player, held, (int) Math.ceil(damageAmount));
                    if (damage > held.getMaxDamage()) StrangeHelper.destroyTotem(player, held);
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
