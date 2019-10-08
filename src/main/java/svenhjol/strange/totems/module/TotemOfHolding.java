package svenhjol.strange.totems.module;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfHoldingItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfHolding extends MesonModule
{
    public static TotemOfHoldingItem item;

    @Config(name = "Save items on death", description = "When you die, your items will be stored in a totem at the place where you died.")
    public static boolean saveItems = true;

    @Config(name = "No despawn", description = "The totem will never despawn.")
    public static boolean noDespawn = true;

    @Config(name = "No gravity", description = "The totem will stay suspended in the air above your death position.")
    public static boolean noGravity = true;

    @Override
    public void init()
    {
        item = new TotemOfHoldingItem(this);
    }

    @SubscribeEvent
    public void onPlayerDrops(LivingDropsEvent event)
    {
        if (!saveItems) return;
        DamageSource source = event.getSource();

        Collection<ItemEntity> drops = event.getDrops();
        if (drops.isEmpty()) return;
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity)event.getEntityLiving();
        World world = player.world;

        ItemStack totem = new ItemStack(item);
        CompoundNBT serialized = new CompoundNBT();
        List<ItemStack> holdable = new ArrayList<>();

        drops.stream()
            .filter(Objects::nonNull)
            .map(ItemEntity::getItem)
            .filter(stack -> !stack.isEmpty())
            .forEach(holdable::add);

        for (int i = 0; i < holdable.size(); i++) {
            serialized.put(Integer.toString(i), holdable.get(i).serializeNBT());
        }

        TotemOfHoldingItem.setItems(totem, serialized);

        if (source != null) {
            TotemOfHoldingItem.setMessage(totem, source.getDeathMessage(event.getEntityLiving()).getString());
        }

        double x = player.posX + 0.5D;
        double y = player.posY + 1.25D;
        double z = player.posZ + 0.5D;

        ItemEntity totemEntity = new ItemEntity(world, x, y, z, totem);

        if (noGravity) {
            totemEntity.setNoGravity(true);
            totemEntity.setMotion(0.0D, 0.0D, 0.0D);
            totemEntity.setPositionAndUpdate(x, y, z);
        }

        if (noDespawn) totemEntity.setNoDespawn();

        totemEntity.setInvulnerable(true);

        world.addEntity(totemEntity);
        event.setCanceled(true);
    }
}
