package svenhjol.strange.feature.runestones.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;
import svenhjol.strange.feature.runestones.common.RunestoneBlockEntity;

public class RunestoneRenderer<R extends RunestoneBlockEntity> implements BlockEntityRenderer<R> {
    @SuppressWarnings("unused")
    public RunestoneRenderer(BlockEntityRendererProvider.Context context) {
        // no context needed.
    }

    /**
     * @see net.minecraft.client.renderer.blockentity.TheEndPortalRenderer
     */
    @Override
    public void render(R runestone, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        if (!runestone.isActivated()) return; // We only want to render the portal texture if the runestone is activated.

        var pose = poseStack.last().pose();
        var vertexConsumer = multiBufferSource.getBuffer(RenderType.endGateway());

        var minMargin = 0.125f;
        var maxMargin = 0.875f;

        renderFace(pose, vertexConsumer, minMargin, maxMargin, minMargin, maxMargin, maxMargin, maxMargin, maxMargin, maxMargin); // South
        renderFace(pose, vertexConsumer, minMargin, maxMargin, maxMargin, minMargin, minMargin, minMargin, minMargin, minMargin); // North
        renderFace(pose, vertexConsumer, maxMargin, maxMargin, maxMargin, minMargin, minMargin, maxMargin, maxMargin, minMargin); // East
        renderFace(pose, vertexConsumer, minMargin, minMargin, minMargin, maxMargin, minMargin, maxMargin, maxMargin, minMargin); // West
    }

    private void renderFace(Matrix4f pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m) {
        vertexConsumer.addVertex(pose, f, h, j);
        vertexConsumer.addVertex(pose, g, h, k);
        vertexConsumer.addVertex(pose, g, i, l);
        vertexConsumer.addVertex(pose, f, i, m);
    }
}
