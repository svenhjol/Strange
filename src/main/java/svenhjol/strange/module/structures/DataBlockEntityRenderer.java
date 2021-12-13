package svenhjol.strange.module.structures;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DataBlockEntityRenderer<T extends DataBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRendererProvider.Context context;

    public DataBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        ItemStack stack = entity.getItem();
        if (stack == null || stack.isEmpty()) return;
        String metadata = entity.getMetadata();
        boolean valid = metadata.startsWith("decoration") || metadata.startsWith("chest") || metadata.startsWith("barrel");

        int rx = 0;
        int ry = 0;

        BlockState state = entity.getBlockState();
        if (state.getBlock() instanceof DataBlock) {
            Direction facing = state.getValue(DataBlock.FACING);
            switch (facing) {
                case EAST -> ry = 270;
                case UP -> rx = 90;
                case DOWN -> rx = 270;
                case SOUTH -> ry = 180;
                case WEST -> ry = 90;
            }
        }

        poseStack.pushPose();
        poseStack.scale(1F, 1F, 1F);
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.mulPose(Vector3f.XP.rotationDegrees(rx));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(ry));

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, 255, valid ? OverlayTexture.NO_OVERLAY : OverlayTexture.RED_OVERLAY_V, poseStack, bufferSource, entity.hashCode());
        poseStack.popPose();
    }
}
