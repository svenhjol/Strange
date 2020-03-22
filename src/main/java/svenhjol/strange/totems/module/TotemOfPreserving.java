package svenhjol.strange.totems.module;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.iface.ITreasureTotem;
import svenhjol.strange.totems.item.TotemOfPreservingItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfPreserving extends MesonModule implements ITreasureTotem {
    public static TotemOfPreservingItem item;

    @Config(name = "Save items on death", description = "When you die, your items will be stored in a totem at the place where you died.")
    public static boolean saveItems = true;

    @Config(name = "No despawn", description = "The totem will never despawn.")
    public static boolean noDespawn = true;

    @Config(name = "No gravity", description = "The totem will stay suspended in the air above your death position.")
    public static boolean noGravity = true;

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init() {
        item = new TotemOfPreservingItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureTotems.availableTotems.add(this);
    }

    @SubscribeEvent
    public void onItemExpire(ItemExpireEvent event) {
        if (noDespawn
            && event.getEntityItem() != null
            && event.getEntityItem().getItem().getItem() == item
            && !TotemOfPreservingItem.getItems(event.getEntityItem().getItem()).isEmpty()
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerDrops(LivingDropsEvent event) {
        if (!saveItems) return;
        DamageSource source = event.getSource();

        Collection<ItemEntity> drops = event.getDrops();
        if (drops.isEmpty()) return;
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
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

        TotemOfPreservingItem.setItems(totem, serialized);

        if (source != null) {
            TotemOfPreservingItem.setMessage(totem, source.getDeathMessage(event.getEntityLiving()).getString());
        }

        final BlockPos playerPos = player.getPosition();

        double x = playerPos.getX() + 0.5D;
        double y = playerPos.getY() + 2.25D;
        double z = playerPos.getZ() + 0.5D;

        if (y < 0) y = 64;

        ItemEntity totemEntity = new ItemEntity(world, x, y, z, totem);

        if (noGravity) {
            totemEntity.setNoGravity(true);
            totemEntity.setMotion(0.0D, 0.0D, 0.0D);
            totemEntity.setPositionAndUpdate(x, y, z);
        }

        if (noDespawn) totemEntity.setNoDespawn();
        totemEntity.setGlowing(true);
        totemEntity.setInvulnerable(true);

        world.addEntity(totemEntity);
        event.setCanceled(true);
        Strange.LOG.debug("Added Totem of Preserving at " + new BlockPos(x, y, z));
    }

    @Override
    public ItemStack getTreasureItem() {
        return new ItemStack(item);
    }
}
