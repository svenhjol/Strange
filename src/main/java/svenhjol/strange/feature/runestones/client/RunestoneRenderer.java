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

        var sideMinMargin = 0.25f;
        var sideMaxMargin = 0.75f;
        var vertMinMargin = 0.1875f;
        var vertMaxMargin = 0.8125f;

        renderFace(pose, vertexConsumer, sideMinMargin, sideMaxMargin, vertMinMargin, vertMaxMargin, sideMaxMargin, sideMaxMargin, sideMaxMargin, sideMaxMargin); // South
        renderFace(pose, vertexConsumer, sideMinMargin, sideMaxMargin, vertMaxMargin, vertMinMargin, sideMinMargin, sideMinMargin, sideMinMargin, sideMinMargin); // North
        renderFace(pose, vertexConsumer, sideMaxMargin, sideMaxMargin, vertMaxMargin, vertMinMargin, sideMinMargin, sideMaxMargin, sideMaxMargin, sideMinMargin); // East
        renderFace(pose, vertexConsumer, sideMinMargin, sideMinMargin, vertMinMargin, vertMaxMargin, sideMinMargin, sideMaxMargin, sideMaxMargin, sideMinMargin); // West
    }

    private void renderFace(Matrix4f pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m) {
        vertexConsumer.addVertex(pose, f, h, j);
        vertexConsumer.addVertex(pose, g, h, k);
        vertexConsumer.addVertex(pose, g, i, l);
        vertexConsumer.addVertex(pose, f, i, m);
    }
}
