package svenhjol.strange.runeportals;

import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

public class RunePortalBlockEntityRenderer<T extends RunePortalBlockEntity> implements BlockEntityRenderer<T> {
//    private static final List<RenderLayer> RENDER_LAYERS = IntStream.range(0, 16).mapToObj((i)
//        -> getEndPortal(i + 1))
//            .collect(Collectors.toList());

    public RunePortalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        // allows client new
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexProvider, int light, int overlay) {
//        int i = 13;
//        float f = 0.75F;
//        Matrix4f matrix4f = matrices.peek().getModel();
//        this.renderSides(entity, f, 0.15F, 0, matrix4f, vertexProvider.getBuffer(RENDER_LAYERS.get(0)));
//
//        for(int j = 1; j < i; ++j) {
//            this.renderSides(entity, f, 2.0F / (float)(18 - j), j, matrix4f, vertexProvider.getBuffer(RENDER_LAYERS.get(j)));
//        }
    }

    private void renderSides(T tile, float offset, float depth, int pass, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        float f;

        if (pass == 0) {
            f = 0.05F;
        } else {
            f = 9.2F / (float) (30 - pass);
        }

        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;

        int colorIndex = tile.color;
        DyeColor dyeColor = DyeColor.byId(colorIndex);
        float[] comps = dyeColor.getColorComponents();
        r = comps[0];
        g = comps[1];
        b = comps[2];

        r *= 1.7F * f;
        g *= 1.7F * f;
        b *= 1.7F * f;

        this.renderFace(tile, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 0.75F, 0.75F, 0.75F, 0.75F, r, g, b, Direction.SOUTH);
        this.renderFace(tile, matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.25F, 0.25F, 0.25F, 0.25F, r, g, b, Direction.NORTH);
        this.renderFace(tile, matrix4f, vertexConsumer, 0.75F, 0.75F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, r, g, b, Direction.EAST);
        this.renderFace(tile, matrix4f, vertexConsumer, 0.25F, 0.25F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, r, g, b, Direction.WEST);
        this.renderFace(tile, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, r, g, b, Direction.DOWN);
        this.renderFace(tile, matrix4f, vertexConsumer, 0.0F, 1.0F, offset, offset, 1.0F, 1.0F, 0.0F, 0.0F, r, g, b, Direction.UP);
    }

    private void renderFace(T entity, Matrix4f matrix4f, VertexConsumer vertices, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float red, float green, float blue, Direction direction) {
        if (entity.shouldDrawSide(direction)) {
            vertices.vertex(matrix4f, f0, f2, f4).color(red, green, blue, 1.0F).next();
            vertices.vertex(matrix4f, f1, f2, f5).color(red, green, blue, 1.0F).next();
            vertices.vertex(matrix4f, f1, f3, f6).color(red, green, blue, 1.0F).next();
            vertices.vertex(matrix4f, f0, f3, f7).color(red, green, blue, 1.0F).next();
        }
    }

    public static RenderLayer getEndPortal(int iteration) {
        RenderPhase.Textures tex1;

        if (iteration <= 1) {
            tex1 = RenderPhase.Textures.create().add(EndPortalBlockEntityRenderer.SKY_TEXTURE, false, false).build();
        } else {
            tex1 = RenderPhase.Textures.create().add(EndPortalBlockEntityRenderer.PORTAL_TEXTURE, false, false).build();
        }

        return RenderLayer.of("rune_portal", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 256, false, false,
            RenderLayer.MultiPhaseParameters.builder()
                .texture(tex1)
                .build(false));
    }
}
