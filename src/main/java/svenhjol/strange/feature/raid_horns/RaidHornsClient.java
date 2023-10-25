package svenhjol.strange.feature.raid_horns;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;

public class RaidHornsClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return RaidHorns.class;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void register() {
        var registry = mod().registry();
        registry.itemProperties("minecraft:tooting",
            RaidHorns.item, () -> this::handleTooting);
    }

    @Override
    public void runWhenEnabled() {
        var registry = mod().registry();
        registry.itemTab(
            RaidHorns.item,
            CreativeModeTabs.TOOLS_AND_UTILITIES,
            Items.TNT_MINECART
        );
    }

    private float handleTooting(ItemStack stack, ClientLevel level, LivingEntity entity, int i) {
        return entity != null
            && entity.isUsingItem()
            && entity.getUseItem() == stack ? 1.0f : 0.0f;
    }
}
