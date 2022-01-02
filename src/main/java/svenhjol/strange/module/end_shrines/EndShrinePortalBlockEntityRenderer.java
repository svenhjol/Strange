package svenhjol.strange.module.end_shrines;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Much copypasta from {@link net.minecraft.client.renderer.blockentity.TheEndPortalRenderer}
 */
public class EndShrinePortalBlockEntityRenderer<T extends EndShrinePortalBlockEntity> implements BlockEntityRenderer<T> {
    public EndShrinePortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource bufferSource, int i, int j) {
        var matrix4f = poseStack.last().pose();
        renderCube(blockEntity, matrix4f, bufferSource.getBuffer(renderType()));
    }

    private void renderCube(T blockEntity, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        var down = this.getOffsetDown();
        var up = this.getOffsetUp();
        renderFace(matrix4f, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH);
        renderFace(matrix4f, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH);
        renderFace(matrix4f, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST);
        renderFace(matrix4f, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST);
        renderFace(matrix4f, vertexConsumer, 0.0f, 1.0f, down, down, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN);
        renderFace(matrix4f, vertexConsumer, 0.0f, 1.0f, up, up, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP);
    }

    private void renderFace(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            vertexConsumer.vertex(matrix4f, f, h, j).endVertex();
            vertexConsumer.vertex(matrix4f, g, h, k).endVertex();
            vertexConsumer.vertex(matrix4f, g, i, l).endVertex();
            vertexConsumer.vertex(matrix4f, f, i, m).endVertex();
        }
    }

    protected float getOffsetUp() {
        return 0.75f;
    }

    protected float getOffsetDown() {
        return 0.375f;
    }

    protected RenderType renderType() {
        return RenderType.endPortal();
    }
}
