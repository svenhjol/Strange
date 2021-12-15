package svenhjol.strange.module.structures;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class EntityBlockEntityRenderer<T extends EntityBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRendererProvider.Context context;
    private final ItemStack stack = new ItemStack(Blocks.PLAYER_HEAD);

    public EntityBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        // don't render the entity decoration if primed (available to trigger)
        if (entity.isPrimed()) return;

        poseStack.pushPose();
        poseStack.scale(1F, 1F, 1F);
        poseStack.translate(0.5F, 0.5F, 0.5F);

        entity.rotateTicks += 0.25F;
        if (entity.rotateTicks >= 360F) {
            entity.rotateTicks = 0F;
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(entity.rotateTicks));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, 255, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.hashCode());
        poseStack.popPose();
    }
}
