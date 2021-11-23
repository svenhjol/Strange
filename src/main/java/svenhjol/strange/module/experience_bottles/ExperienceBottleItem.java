package svenhjol.strange.module.experience_bottles;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.colored_glints.ColoredGlints;

public class ExperienceBottleItem extends CharmItem {
    private final ExperienceBottles.Type type;

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

    public ExperienceBottleItem(CharmModule module, ExperienceBottles.Type type) {
        super(module, type.getSerializedName() + "_experience_bottle", new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .rarity(type.getRarity())
            .stacksTo(16));

        this.type = type;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (enabled()) {
            ItemStack stack = new ItemStack(this);
            ColoredGlints.applyColoredGlint(stack, type.getColor().getSerializedName());
            items.add(stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // a lot of this is from the vanilla ExperienceBottleItem
        ItemStack held = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ExperienceBottleProjectile bottle = new ExperienceBottleProjectile(level, player);
            bottle.setItem(held);
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -20F, 0.7F, 1.0F);
            level.addFreshEntity(bottle);
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    public ExperienceBottles.Type getType() {
        return type;
    }
}
