package svenhjol.strange.module.structure_triggers;

import com.mojang.blaze3d.vertex.PoseStack;
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
//        int rx = 0;
//        int ry = 0;

        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.translate(0.325F, 0.325F, 0.325F);
//
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(rx));
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(ry));

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, 255, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.hashCode());
        poseStack.popPose();
    }
}
