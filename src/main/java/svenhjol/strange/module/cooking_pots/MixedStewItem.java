package svenhjol.strange.module.cooking_pots;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.stackable_stews.StackableStews;

import java.util.List;

public class MixedStewItem extends CharmItem {
    public static final String TAG_SATURATION = "Saturation";

    public MixedStewItem(CharmModule module) {
        super(module, "mixed_stew", (new Properties())
            .stacksTo(StackableStews.stackSize)
            .tab(CreativeModeTab.TAB_FOOD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack held = user.getItemInHand(hand);
        if (user.canEat(false)) {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(held);
        }
        return InteractionResultHolder.pass(held);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // vanilla non-snack ticks
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        level.gameEvent(user, GameEvent.EAT, user.eyeBlockPosition());
        level.playSound(null, user.getX(), user.getY(), user.getZ(), user.getEatingSound(stack), SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);

        if (user instanceof Player player) {
            player.getFoodData().eat(CookingPots.hungerRestored, getSaturation(stack));
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            getEffects(stack).forEach(player::addEffect);

            if (!level.isClientSide) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, stack);
            }

            stack.shrink(1);
            player.addItem(new ItemStack(Items.BOWL));

            user.gameEvent(GameEvent.EAT);
        }

        return stack;
    }

    public static List<MobEffectInstance> getEffects(ItemStack stack) {
        return PotionUtils.getMobEffects(stack);
    }

    public static void setEffects(ItemStack stack, List<MobEffectInstance> effects) {
        PotionUtils.setCustomEffects(stack, effects);
    }

    public static float getSaturation(ItemStack stack) {
        return stack.getOrCreateTag().getFloat(TAG_SATURATION);
    }

    public static void setSaturation(ItemStack stack, float saturation) {
        stack.getOrCreateTag().putFloat(TAG_SATURATION, saturation);
    }

    public static int getQuality(ItemStack stack) {
        float quality = getSaturation(stack);
        if (quality > 0) {
            quality /= 2.0F;
        }

        if (quality % 2 == 1) {
            quality -= 1;
        }

        return (int)Mth.clamp(quality, 0, 8);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        int quality = getQuality(stack);
        tooltip.add(new TranslatableComponent("item.strange.mixed_stew.quality" + quality).withStyle(ChatFormatting.GOLD));

        ClientHelper.getClient().ifPresent(client -> {
            if (client.options.advancedItemTooltips) {
                tooltip.add(new TranslatableComponent("item.strange.mixed_stew.saturation", getSaturation(stack)).withStyle(ChatFormatting.GOLD));
            }
        });

        PotionUtils.addPotionTooltip(stack, tooltip, 0.25F);
    }
}
