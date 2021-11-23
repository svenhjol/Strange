package svenhjol.strange.module.glowballs;

import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import svenhjol.charm.item.ICharmItem;
import svenhjol.charm.loader.CharmModule;

public class GlowballItem extends EnderpearlItem implements ICharmItem {
    protected CharmModule module;

    public GlowballItem(CharmModule module) {
        super(new Properties().stacksTo(16).tab(CreativeModeTab.TAB_MISC));
        this.module = module;
        this.register(module, "glowball");
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        if (enabled())
            super.fillItemCategory(group, stacks);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            GlowballEntity entity = new GlowballEntity(level, player);
            entity.setItem(held);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(entity);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    @Override
    public boolean enabled() {
        return module.isEnabled();
    }
}
