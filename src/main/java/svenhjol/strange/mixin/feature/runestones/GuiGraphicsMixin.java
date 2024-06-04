package svenhjol.strange.mixin.feature.runestones;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.runestones.RunestonesClient;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Shadow @Final private PoseStack pose;

    @Inject(
        method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
            shift = At.Shift.AFTER
        )
    )
    private void hookAddPoseScale(LivingEntity livingEntity, Level level, ItemStack stack, int i, int j, int k, int l, CallbackInfo ci) {
        Resolve.feature(RunestonesClient.class).handlers.scaleItem(stack, this.pose);
    }
}
