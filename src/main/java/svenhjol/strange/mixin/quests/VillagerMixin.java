package svenhjol.strange.mixin.quests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import svenhjol.strange.feature.quests.Quests;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(
        method = "mobInteract",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hookMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof ServerPlayer serverPlayer && Quests.tryComplete(serverPlayer, (Villager)(Object)this)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
