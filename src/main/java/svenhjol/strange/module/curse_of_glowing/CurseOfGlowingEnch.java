package svenhjol.strange.module.curse_of_glowing;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import svenhjol.charm.enchantment.CharmEnchantment;
import svenhjol.charm.loader.CharmModule;

public class CurseOfGlowingEnch extends CharmEnchantment {
    public CurseOfGlowingEnch(CharmModule module) {
        super(module, "glowing_curse", Rarity.UNCOMMON, EnchantmentCategory.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int i) {
        return 10;
    }

    @Override
    public int getMaxCost(int i) {
        return 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }
}
