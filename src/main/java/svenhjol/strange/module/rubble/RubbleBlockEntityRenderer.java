package svenhjol.strange.module.rubble;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RubbleBlockEntityRenderer<T extends RubbleBlockEntity> implements BlockEntityRenderer<T> {
    public RubbleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // allows client new
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Level world = entity.getLevel();
        if (world == null)
            return;

        ItemStack stack = entity.getItemStack();
        if (stack == null || stack.isEmpty())
            return;

        matrices.pushPose();
        matrices.scale(0.62F, 0.62F, 0.62F);
        matrices.translate(0.75F, 0.75F, 0.75F);

        matrices.mulPose(Vector3f.XP.rotationDegrees((float)entity.hashCode() % 45.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees((float)entity.hashCode() % 360.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees((float)entity.hashCode() % 45.0F));

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, entity.hashCode());
        matrices.popPose();
    }
}
