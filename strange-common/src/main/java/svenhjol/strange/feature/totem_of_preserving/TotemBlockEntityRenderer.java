package svenhjol.strange.feature.totem_of_preserving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TotemBlockEntityRenderer<T extends TotemBlockEntity> implements BlockEntityRenderer<T> {
    private final ItemStack stack;

    public TotemBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.stack = new ItemStack(TotemOfPreserving.ITEM.get());
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.scale(1F, 1F, 1F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);

        var minecraft = Minecraft.getInstance();
        var itemRenderer = minecraft.getItemRenderer();
        if (itemRenderer == null) return;

        entity.rotateTicks += 0.25F;
        if (entity.rotateTicks >= 360F) {
            entity.rotateTicks = 0F;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.rotateTicks));
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 255, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, minecraft.level, entity.hashCode());
        poseStack.popPose();
    }
}