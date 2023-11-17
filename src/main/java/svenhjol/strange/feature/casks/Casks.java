package svenhjol.strange.feature.casks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charmony.annotation.Configurable;
import svenhjol.charmony.common.CommonFeature;

import java.util.List;
import java.util.function.Supplier;

public class Casks extends CommonFeature {
    static final String BLOCK_ID = "cask";
    static Supplier<CaskBlock> block;
    static Supplier<CaskBlock.BlockItem> blockItem;
    static Supplier<BlockEntityType<CaskBlockEntity>> blockEntity;
    static Supplier<SoundEvent> addSound;
    static Supplier<SoundEvent> emptySound;
    static Supplier<SoundEvent> nameSound;
    static Supplier<SoundEvent> takeSound;

    @Configurable(name = "Maximum bottles", description = "Maximum number of bottles a cask can hold.")
    public static int maxPortions = 64;

    @Configurable(name = "Allow splash and lingering", description = "If true, splash and lingering potions may be added to a cask.")
    public static boolean allowSplashAndLingering = false;

    @Override
    public void register() {
        var registry = mod().registry();

        block = registry.block(BLOCK_ID, () -> new CaskBlock(this));
        blockItem = registry.item(BLOCK_ID, () -> new CaskBlock.BlockItem(block));
        blockEntity = registry.blockEntity(BLOCK_ID, () -> CaskBlockEntity::new, List.of(block));

        // Casks can be burned in a furnace
        registry.fuel(block);

        // Network packets
        CasksNetwork.register(registry);

        // Sounds
        addSound = registry.soundEvent("cask_add");
        emptySound = registry.soundEvent("cask_empty");
        nameSound = registry.soundEvent("cask_name");
        takeSound = registry.soundEvent("cask_take");
    }

    public static ItemStack getFilledWaterBottle(int amount) {
        var out = new ItemStack(Items.POTION, amount);
        PotionUtils.setPotion(out, Potions.WATER);
        return out;
    }

    public static boolean isValidPotion(ItemStack potion) {
        boolean valid = potion.is(Items.POTION);

        if (!valid && allowSplashAndLingering) {
            valid = potion.is(Items.LINGERING_POTION) || potion.is(Items.SPLASH_POTION);
        }

        return valid;
    }
}
