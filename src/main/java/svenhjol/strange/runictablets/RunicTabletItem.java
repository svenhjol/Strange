package svenhjol.strange.runictablets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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
import svenhjol.strange.module.Foundations;

import javax.annotation.Nullable;

public class RunicTabletItem extends CharmItem {
    public static final String ORIGIN_TAG = "origin";
    public static final String POS_TAG = "pos";

    public RunicTabletItem(CharmModule module) {
        super(module, "runic_tablet", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.RARE)
            .maxCount(1)
            .maxDamage(10));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack tablet = user.getStackInHand(hand);

        if (!DimensionHelper.isOverworld(world)) {
            user.sendMessage(new TranslatableText("runictablet.strange.wrong_dimension"), true);
            return TypedActionResult.fail(tablet);
        }

        if (!world.isClient) {
            BlockPos pos = getPos(tablet);
            BlockPos origin = getOrigin(tablet);

            if (origin == null)
                origin = user.getBlockPos();

            if (pos == null) {
                BlockPos shiftPos = PosHelper.addRandomOffset(origin, world.random, 6000);
                setPos(tablet, shiftPos);

                BlockPos structurePos = ((ServerWorld) world).locateStructure(Foundations.FEATURE, shiftPos, 1500, false);

                if (structurePos == null) {
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.75F, 1.2F);
                    return TypedActionResult.fail(ItemStack.EMPTY);
                }

                setPos(tablet, structurePos);
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

        BlockPos pos = getPos(stack);

        if (!DimensionHelper.isOverworld(world))
            return;

        if (pos == null)
            return;

        pos = PosHelper.getSurfacePos(world, pos);
        if (pos == null)
            return;

        user.teleport(pos.getX(), pos.getY(), pos.getZ(), true);
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
    public static BlockPos getOrigin(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(ORIGIN_TAG))
            return null;

        return BlockPos.fromLong(tablet.getOrCreateTag().getLong(ORIGIN_TAG));
    }

    @Nullable
    public static BlockPos getPos(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(tablet.getOrCreateTag().getLong(POS_TAG));
    }

    public static void setOrigin(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(ORIGIN_TAG, pos.asLong());
    }

    public static void setPos(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }
}
