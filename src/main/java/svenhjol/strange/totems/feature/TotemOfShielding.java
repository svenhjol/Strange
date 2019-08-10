package svenhjol.strange.totems.feature;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import svenhjol.meson.Feature;
import svenhjol.strange.totems.item.TotemOfShieldingItem;

public class TotemOfShielding extends Feature
{
    public static ForgeConfigSpec.ConfigValue<Integer> maxHealth;
    public static ForgeConfigSpec.ConfigValue<Double> damageMultiplier;
    public static TotemOfShieldingItem item;

    @Override
    public void configure()
    {
        super.configure();

        maxHealth = builder
            .comment("Durability of the Totem.")
            .define("Maximum Health", 100);

        damageMultiplier = builder
            .comment("The received player damage is multiplied by this amount before being transferred to the Totem.")
            .define("Damage multiplier", 0.75D);
    }

    @Override
    public void init()
    {
        super.init();
        item = new TotemOfShieldingItem();
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
                double damage = event.getAmount() * damageMultiplier.get();
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

    @Override
    public boolean hasSubscriptions()
    {
        return true;
    }

    @Override
    public void onRegisterItems(IForgeRegistry<Item> registry)
    {
        registry.register(item);
    }
}
