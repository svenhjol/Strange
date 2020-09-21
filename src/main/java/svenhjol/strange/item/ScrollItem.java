package svenhjol.strange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.item.MesonItem;

public class ScrollItem extends MesonItem {
    private static final String DEFINITION_TAG = "definition";
    private static final String RARITY_TAG = "rarity";

    private final int tier;

    public ScrollItem(MesonModule module, int tier) {
        super(module, "scroll_tier_" + tier, new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1)
        );

        this.tier = tier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);
    }
}
