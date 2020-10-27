package svenhjol.strange.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.item.CharmItem;

import javax.annotation.Nullable;

public class RunicTabletItem extends CharmItem {
    public static final String POS_TAG = "pos";
    public static final String DIMENSION_TAG = "dimension";
    public static final String EXACT_TAG = "exact";

    public RunicTabletItem(CharmModule module, String name) {
        super(module, name, new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1)
            .maxDamage(10));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        BlockPos pos = getPos(stack);
        if (pos == null)
            return TypedActionResult.fail(stack);

        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.75F, 1.2F);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient)
            return;

        BlockPos pos = getPos(stack);
        Identifier dimension = getDimension(stack);
        boolean exact = getExact(stack);

        if (!DimensionHelper.isDimension(world, dimension))
            return;

        if (pos == null) {
            Charm.LOG.warn("Encoded position was null");
            return;
        }

        if (!exact) {
            pos = PosHelper.getSurfacePos(world, pos);
            if (pos == null)
                return;
        }

        user.teleport(pos.getX(), pos.getY(), pos.getZ(), true);
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.25F);
        stack.damage(1, user, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 40000;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Nullable
    public static Identifier getDimension(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(DIMENSION_TAG))
            return null;

        return new Identifier(tablet.getOrCreateTag().getString(DIMENSION_TAG));
    }

    @Nullable
    public static BlockPos getPos(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(tablet.getOrCreateTag().getLong(POS_TAG));
    }

    public static boolean getExact(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(EXACT_TAG))
            return false;

        return tablet.getOrCreateTag().getBoolean(EXACT_TAG);
    }

    public static void setDimension(ItemStack tablet, Identifier dimension) {
        tablet.getOrCreateTag().putString(DIMENSION_TAG, dimension.toString());
    }

    public static void setPos(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }

    public static void setExact(ItemStack tablet, boolean exact) {
        tablet.getOrCreateTag().putBoolean(EXACT_TAG, exact);
    }
}
