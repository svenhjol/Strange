package svenhjol.strange.mixin.villager_tweaks;

import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.feature.villager_tweaks.VillagerTweaks;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(
        method = "isTradeable",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookIsTradeable(CallbackInfoReturnable<Boolean> cir) {
        Enchantment enchantment = (Enchantment)(Object)this;
        if (VillagerTweaks.shouldNotTrade(enchantment)) {
            cir.setReturnValue(false);
        }
    }
}