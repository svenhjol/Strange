package svenhjol.strange.mixin.relics;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import svenhjol.strange.Strange;
import svenhjol.strange.module.relics.Relics;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class PreserveHighestLevelEnchantmentsMixin extends ItemCombinerMenu {
    public PreserveHighestLevelEnchantmentsMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(type, syncId, playerInventory, context);
    }

    /**
     * When trying to combine two items on the anvil where at least one of the items
     * has an enchantment level higher than the maximum, the additional levels are
     * lost and the output item defaults to the enchantment maximum.
     *
     * This mixin redirects the set call to the EnchantmentHelper to a custom one
     * provided by AnvilImprovements.
     */
    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;setEnchantments(Ljava/util/Map;Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void hookCreateResult(Map<Enchantment, Integer> enchantments, ItemStack outputStack) {
        if (!Strange.LOADER.isEnabled(Relics.class)) {
            EnchantmentHelper.setEnchantments(enchantments, outputStack); // vanilla behavior
            return;
        }

        ItemStack inputStack = this.inputSlots.getItem(1);
        Relics.preserveHighestLevelEnchantment(enchantments, inputStack, outputStack);
    }
}
