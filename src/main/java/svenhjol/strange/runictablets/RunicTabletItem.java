package svenhjol.strange.runictablets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.item.CharmItem;

import javax.annotation.Nullable;

public class RunicTabletItem extends CharmItem {
    public static final String POS_TAG = "pos";

    public RunicTabletItem(CharmModule module) {
        super(module, "runic_tablet", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.RARE)
            .maxCount(1)
            .maxDamage(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack tablet = user.getStackInHand(hand);

        // TODO: runic tablets should have a dimension tag
        if (!DimensionHelper.isOverworld(world)) {
            user.sendMessage(new TranslatableText("runictablet.strange.wrong_dimension"), true);
            return TypedActionResult.fail(tablet);
        }

        if (!world.isClient) {
            BlockPos pos = getPos(tablet);

            if (pos == null) {
                world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.75F, 1.2F);
                return TypedActionResult.fail(tablet);
            }

            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.75F, 1.2F);
            user.setCurrentHand(hand);
        }

        return TypedActionResult.consume(tablet);
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

        if (getPullProgress(this.getMaxUseTime(stack) - remainingUseTicks) < 0.9D)
            return;

        BlockPos pos = getPos(stack);

        if (!DimensionHelper.isOverworld(world))
            return;

        if (pos == null)
            return;

        if (pos.getY() == 0) {
            pos = PosHelper.getSurfacePos(world, pos);
            if (pos == null)
                return;
        }


        user.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.5F, 1.25F);
        stack.damage(1, user, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Nullable
    public static BlockPos getPos(ItemStack tablet) {
        if (tablet.getTag() == null || !tablet.getTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(tablet.getOrCreateTag().getLong(POS_TAG));
    }

    public static void setPos(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }

    public static float getPullProgress(int useTicks) {
        float f = (float)useTicks / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
}
