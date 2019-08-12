package svenhjol.strange.totems.feature;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import svenhjol.meson.Feature;
import svenhjol.strange.totems.item.TotemOfAttractingItem;

import java.util.List;

public class TotemOfAttracting extends Feature
{
    public static TotemOfAttractingItem item;
    public static ForgeConfigSpec.ConfigValue<Integer> maxHealth;
    public static ForgeConfigSpec.ConfigValue<Integer> range;

    @Override
    public void configure()
    {
        super.configure();

        maxHealth = builder
            .comment("Durability of the Totem.")
            .define("Maximum Health", 100);

        range = builder
            .comment("Drops within this range of the player will be automatically picked up.")
            .define("Attraction range", 6);
    }

    @Override
    public void init()
    {
        super.init();

        item = new TotemOfAttractingItem();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START
            && event.side.isServer()
            && event.player.getHeldItemOffhand().getItem() == item
        ) {
            int r = range.get();
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
