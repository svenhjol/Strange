package svenhjol.strange.totems.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.base.TotemHelper;
import svenhjol.strange.totems.item.TotemOfAttractingItem;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfAttracting extends MesonModule
{
    public static TotemOfAttractingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 128;

    @Config(name = "Attract items", description = "If true, the totem will passively pick up items around the player when the totem is held.")
    public static boolean attractItems = true;

    @Config(name = "Attract items range", description = "If attract items is enabled, items within this range of the player will be picked up.")
    public static int attractRange = 10;

    @Config(name = "Attract mobs", description = "If true, the totem will attract nearby mobs to the player when the totem is held.")
    public static boolean attractMobs = true;

    @Config(name = "Detect block", description = "If true, the totem can be bound to a block and will then detect blocks of the same type.")
    public static boolean detectBlock = true;

    @Config(name = "Detect block range", description = "Range (in blocks) that the totem will be attracted to the bound block.")
    public static int detectRange = 24;

    @Config(name = "Chance of damage", description = "Chance (out of 1.0) of damaging the totem while passively working (every 5 ticks).")
    public static double damageChance = 0.02D;

    @Override
    public void init()
    {
        item = new TotemOfAttractingItem(this);
    }

    @SubscribeEvent
    public void onRightClick(RightClickBlock event)
    {
        if (!detectBlock) return;

        PlayerEntity player = event.getPlayer();
        if (player == null || !player.isAlive()) return;

        ItemStack held = player.getHeldItem(event.getHand());
        if (!(held.getItem() instanceof TotemOfAttractingItem)) return;

        World world = event.getWorld();

        if (player.isSneaking()) {
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);
            TotemOfAttractingItem.setLinkedBlock(held, state.getBlock());

            if (world.isRemote) {
                effectBinding(pos);
                player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.8F);
            }

        } else {

            BlockPos pos = player.getPosition();
            Block block = TotemOfAttractingItem.getLinkedBlock(held);
            if (block == null) return; // must be bound to block before trying to detect it

            AtomicReference<Double> distance = new AtomicReference<>(0.0D);
            AtomicLong foundPos = new AtomicLong();
            Stream<BlockPos> inRange = BlockPos.getAllInBox(pos.add(-detectRange, -detectRange, -detectRange), pos.add(detectRange, detectRange, detectRange));
            inRange.forEach(p -> {
                if (world.getBlockState(p).getBlock() != block) return;
                double d = getDistance(pos, p);
                if (distance.get() == 0.0D || d < distance.get()) {
                    distance.set(d);
                    foundPos.set(p.toLong());
                }
            });

            long l = foundPos.get();
            if (l == 0.0D) return;

            BlockPos found = BlockPos.fromLong(l);
            player.lookAt(EntityAnchorArgument.Type.EYES, new Vec3d(found).add(0.5F, 0.5F, 0.5F));
            TotemHelper.damageOrDestroy(player, held, 1);
            if (world.isRemote) {
                double pitch = Math.max(0.5D, 0.5D + ((detectRange - distance.get()) / detectRange));
                player.playSound(StrangeSounds.ATTRACTED, 1.0F, (float)pitch);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (!attractItems) return;

        if (event.phase == TickEvent.Phase.START
            && event.side.isServer()
            && event.player.world.getGameTime() % 5 == 0
            && (event.player.getHeldItemMainhand().getItem() == item
            || event.player.getHeldItemOffhand().getItem() == item)
        ) {
            PlayerEntity player = event.player;
            World world = player.world;
            int r = attractRange;
            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;
            ItemStack held = null;

            for (Hand hand : Hand.values()) {
                held = player.getHeldItem(hand);
            }
            if (held == null) return;

            // damage the totem
            if (!world.isRemote && world.rand.nextFloat() < damageChance) {
                TotemHelper.damageOrDestroy(player, held, 1);
            }

            List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(
                x - r, y - r, z - r, x + r, y + r, z + r));

            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if (item.getItem().isEmpty() || !item.isAlive()) continue;
                    item.setPosition(x, y, z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingUpdateEvent event)
    {
        if (!attractMobs) return;

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

    @OnlyIn(Dist.CLIENT)
    public static void effectBinding(BlockPos src)
    {
        double spread = 0.7D;
        for (int i = 0; i < 8; i++) {
            double px = src.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = src.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = src.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.08D, 0.0D);
        }
    }

    private static float getDistance(BlockPos pos1, BlockPos pos2)
    {
        return getDistance(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());
    }

    private static float getDistance(int x1, int z1, int x2, int z2)
    {
        int x = x2 - x1;
        int z = z2 - z1;
        return MathHelper.sqrt((float)(x * x + z * z));
    }
}
