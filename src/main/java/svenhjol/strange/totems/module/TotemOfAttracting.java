package svenhjol.strange.totems.module;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.TotemHelper;
import svenhjol.strange.totems.item.TotemOfAttractingItem;

import java.util.List;
import java.util.stream.Collectors;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfAttracting extends MesonModule
{
    public static TotemOfAttractingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 128;

    @Config(name = "Attraction range", description = "Drops within this range of the player will be automatically picked up.")
    public static int range = 10;

    @Config(name = "Chance of damage", description = "Chance (out of 1.0) of damaging the totem while using it (every 5 ticks).")
    public static double damageChance = 0.1D;

    @Override
    public void init()
    {
        item = new TotemOfAttractingItem(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START
            && event.side.isServer()
            && event.player.world.getGameTime() % 5 == 0
            && (event.player.getHeldItemMainhand().getItem() == item
            || event.player.getHeldItemOffhand().getItem() == item)
        ) {
            PlayerEntity player = event.player;
            World world = player.world;
            int r = range;
            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;

            List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(
                x - r, y - r, z - r, x + r, y + r, z + r));

            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if (item.getItem().isEmpty() || item.isAlive()) continue;
                    item.setPosition(x, y, z);
                }

                if (!world.isRemote
                    && (!player.isCreative() || !player.isSpectator())
                    && world.rand.nextFloat() < 0.2F
                ) {
                    for (Hand hand : Hand.values()) {
                        ItemStack held = player.getHeldItem(hand);
                        int damage = TotemHelper.damage(player, held, 1);

                        if (damage > held.getMaxDamage()) {
                            TotemHelper.destroy(player, held);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingUpdateEvent event)
    {
        if (event.getEntityLiving().world.getGameTime() % 20 == 0
            && event.getEntityLiving() instanceof MobEntity
            && event.getEntityLiving() instanceof CreatureEntity
            && ((MobEntity)event.getEntityLiving()).getNavigator() instanceof GroundPathNavigator
        ) {
            MobEntity entity = (MobEntity)event.getEntityLiving();
            List<PrioritizedGoal> goals = entity.goalSelector.getRunningGoals().collect(Collectors.toList());

            if (goals.isEmpty()) return;
            int size = goals.size();

            PrioritizedGoal goal = goals.get(size - 1);

            if (!(goal.getGoal() instanceof TemptGoal)) {
                entity.goalSelector.addGoal(size, new TemptGoal((CreatureEntity)entity, 1.25D, Ingredient.fromItems(item), false));
            }
        }
    }
}
