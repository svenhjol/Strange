package svenhjol.strange.module.runic_tomes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.state.BlockState;

public class RunicLecternRenderer<T extends RunicLecternBlockEntity> implements BlockEntityRenderer<T> {
    private final BookModel bookModel;
    private final BlockEntityRendererProvider.Context context;

    public RunicLecternRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.context = context;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        // copypasta from LecternRenderer#render
        BlockState state = blockEntity.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0625D, 0.5D);
        float g = state.getValue(RunicLecternBlock.FACING).getClockWise().toYRot();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-g));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0D, -0.125D, 0.0D);
        this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
        VertexConsumer vertexConsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
