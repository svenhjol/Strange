package svenhjol.strange.totems.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfShieldingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfShielding extends MesonModule
{
    public static TotemOfShieldingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 120;

    @Config(name = "Damage multiplier", description = "Player damage is multiplied by this amount before being transferred to the Totem." )
    public static double damageMultiplier;

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
            ItemStack held = player.getHeldItemOffhand();

            if (held.getItem() instanceof TotemOfShieldingItem) {
                double damage = event.getAmount() * damageMultiplier;
                boolean dead = held.attemptDamageItem((int)Math.ceil(damage), player.world.rand, (ServerPlayerEntity)player);

                if (dead) {
                    held.shrink(1);
                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8F, 1.0F);
                } else {
                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.25F);
                }

                event.setCanceled(true);
            }
        }
    }
}
