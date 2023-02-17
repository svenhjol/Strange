package svenhjol.strange.mixin.villager_tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.feature.villager_tweaks.VillagerTweaks;

@Mixin(FarmBlock.class)
public class FarmBlockMixin {
    @Inject(
        method = "fallOn",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookOnLandedUpon(Level level, BlockState state, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
        if (VillagerTweaks.noCropTrampling && (VillagerTweaks.NO_TRAMPLING.contains(entity.getType()))) {
            ci.cancel();
        }
    }
}
