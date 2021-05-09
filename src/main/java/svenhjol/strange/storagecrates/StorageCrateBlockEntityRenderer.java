package svenhjol.strange.storagecrates;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public class StorageCrateBlockEntityRenderer<T extends StorageCrateBlockEntity> implements BlockEntityRenderer<T> {
    protected BlockEntityRendererFactory.Context context;
    private Map<T, Float> entityTicks = new HashMap<>();
    private static AbstractTexture mippedBlocks;
    private static Map<Item, NativeImageBackedTexture> cachedTextures = new WeakHashMap<>();

    public StorageCrateBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public boolean rendersOutsideBoundingBox(T blockEntity) {
        return false;
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null)
            return;

        int count = entity.count;
        Item item = entity.item;
        if (item == null)
            return;

        ItemStack stack = new ItemStack(item);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();


        int distCutoffRender = 74;
        int distFullRender = 16;

        int x = entity.getPos().getX();
        int y = entity.getPos().getY();
        int z = entity.getPos().getZ();

        BlockEntityRenderDispatcher dispatcher = this.context.getRenderDispatcher();
        Camera camera = dispatcher.camera;
        double d = camera.getPos().squaredDistanceTo(x, y, z);


        if (!entityTicks.containsKey(entity))
            entityTicks.put(entity, 0.0F);

        float ticks = entityTicks.get(entity);

        if (ticks > 360.0F)
            ticks = 0.0F;

        Random random = new Random();
        random.setSeed((long) entity.hashCode() + count);


        double[] coords;
        int layers;
        int remainder;

        if (count <= 6) {
            layers = 1;
            remainder = count;
        } else if (count <= 12) {
            layers = 2;
            remainder = count - 6;
        } else {
            layers = 3;
            remainder = count - 12;
        }

        for (int l = 0; l < layers; l++) {
            int layerLight = light >> l;
            float t = l % 2 == 0 ? 360.0F - ticks : ticks;

            if (remainder == 1) {
                coords = new double[]{0.45D, 0.45D};
            } else if (remainder == 2) {
                coords = new double[]{0.3D, 0.3D, 0.6D, 0.6D};
            } else if (remainder == 3) {
                coords = new double[]{0.5D, 0.3D, 0.3D, 0.6D, 0.6D, 0.6D};
            } else if (remainder == 4) {
                coords = new double[]{0.3D, 0.3D, 0.6D, 0.3D, 0.3D, 0.6D, 0.6D, 0.6D};
            } else if (remainder == 5) {
                coords = new double[]{0.5D, 0.5D, 0.25D, 0.25D, 0.75D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D};
            } else {
                coords = new double[]{0.25D, 0.25D, 0.5D, 0.25D, 0.75D, 0.25D, 0.25D, 0.75D, 0.5D, 0.75D, 0.75D, 0.75D};
            }

            double xo = 0;
            double yo = (layers - l) * 0.55F;
            double zo = 0;
            double h = 0;
            for (int i = 0; i < coords.length; i++) {
                if (i % 2 == 0) {
                    xo = coords[i];
                } else {
                    zo = coords[i];

                    renderItemStack(world, itemRenderer, stack, xo, yo + (h * 0.01D), zo, 0.5F - (Math.min(6, remainder) * 0.01F), t, matrices, vertexConsumers, layerLight);
                }
                ++h;
            }

            remainder = count - (l * 6);
        }

        entityTicks.put(entity, ticks + 0.05F);


//        BakedModel model = itemRenderer.getHeldItemModel(stack, world, null, 1414);
//
//        if (mippedBlocks == null) {
//            mippedBlocks = new AbstractTexture() {
//
//                @Override
//                public void load(ResourceManager manager) throws IOException {
//                    clearGlId();
//                    SpriteAtlasTexture atlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(new Identifier("textures/atlas/blocks.png"));
//                    GlStateManager._bindTexture(atlas.getGlId());
//                    int maxLevel = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL);
//                    if (maxLevel == 0 || !GL.getCapabilities().GL_ARB_copy_image) {
//                        int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
//                        int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
//                        ByteBuffer dest = MemoryUtil.memAlloc(w*h*4);
//                        try {
//                            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, NativeImage.Format.ABGR.getPixelDataFormat(), GL11.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(dest));
//                        } catch (Error | RuntimeException e) {
//                            MemoryUtil.memFree(dest);
//                            throw e;
//                        }
//                        NativeImage img = NativeImageAccessor.invokeConstructor(NativeImage.Format.ABGR, w, h, false, MemoryUtil.memAddress(dest));
//                        try {
//                            NativeImage mipped = MipmapHelper.getMipmapLevelsImages(img, 0)[0];
//                            try {
//                                TextureUtil.prepareImage(getGlId(), mipped.getWidth(), mipped.getHeight());
//                                GlStateManager._bindTexture(getGlId());
//                                mipped.upload(0, 0, 0, true);
//                            } finally {
//                                mipped.close();
//                            }
//                        } finally {
//                            img.close();
//                        }
//                    } else {
//                        int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_WIDTH);
//                        int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_HEIGHT);
//                        TextureUtil.prepareImage(getGlId(), w, h);
//                        ARBCopyImage.glCopyImageSubData(
//                            atlas.getGlId(), GL11.GL_TEXTURE_2D, 1, 0, 0, 0,
//                            getGlId(), GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
//                            w, h, 1);
//                    }
//                }
//            };
//            MinecraftClient.getInstance().getTextureManager().registerTexture(new Identifier("fabrication", "textures/atlas/blocks-mip.png"), mippedBlocks);
//        }
//
//
//        matrices.push();
//        matrices.scale(0.55F, 0.55F, 0.55F);
//        RenderLayer defLayer = RenderLayers.getItemLayer(stack, true);
//        RenderLayer layer = defLayer == TexturedRenderLayers.getEntityCutout() ?
//            RenderLayer.getEntityCutout(new Identifier("fabrication", "textures/atlas/blocks-mip.png")) :
//            RenderLayer.getEntityTranslucent(new Identifier("fabrication", "textures/atlas/blocks-mip.png"));
//        VertexConsumer vertices = vertexConsumers.getBuffer(layer);
//
//        ((ItemRendererAccessor)itemRenderer).invokeRenderBakedItemModel(model, stack, light, overlay, matrices, vertices);
//        matrices.pop();

//
//        boolean isBlockItem = item instanceof BlockItem && ((BlockItem)item).getBlock().getDefaultState().isSolidBlock(world, BlockPos.ORIGIN);
//        float scaleSize = isBlockItem ? 0.55F : 0.44F;
//        double scaleLayerHeight = isBlockItem ? 0.17D : 0.15D;
//
//
//        if (d < distCutoffRender) {
//            ItemStack stack = new ItemStack(item);
//
//            int maxLayers = isBlockItem ? 4 : 2;
//            int itemsPerLayer = isBlockItem ? 4 : 8;
//
//            int t = maxLayers * itemsPerLayer;
//            boolean firstpass = count <= t;
//
//            int maxCount = firstpass ? count : (t + 1 - itemsPerLayer) + ((count - 1) % itemsPerLayer);
//
//            int layer = 0;
//            int seed = 0;
//
//            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
//            Random random = new Random();
//
//            for (int i = 0; i < t; i++) {
//                if (i % itemsPerLayer == 0) {
//                    layer = layer + 1;
//                }
//
//                seed = firstpass ? layer : layer + (int) Math.ceil(((double) (count - t) / itemsPerLayer));
//                random.setSeed((long) entity.hashCode() * (seed + 1));
//
//                int layerLight = (light >> (firstpass ? ((count - 1) / itemsPerLayer) / layer : maxLayers - layer)) - 1;
//
//                matrices.push();
//                matrices.translate(0.0F, 0.1D + (layer * scaleLayerHeight), 0.0F);
//
//                int f = maxCount - i;
////                int k = (firstpass || layer >= maxLayers - 2) ? 1 : 2;
//
//
//                int k = 1;
//                if (d > distFullRender && !firstpass && layer < maxLayers)
//                    k = 4;
//
//                float s = scaleSize - (0.02F * (maxLayers - layer));
//
//                for (int j = 0; j < Math.min(itemsPerLayer, f); j = j + k) {
//                    matrices.push();
//                    matrices.translate(0.2D + random.nextDouble() * 0.6D, 0.0D, 0.2D + random.nextDouble() * 0.6D);
//                    matrices.scale(s, s, s);
//                    matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
//                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-30 + (random.nextFloat() * 60)));
//                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-180 + (random.nextFloat() * 360)));
//                    itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, layerLight, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.hashCode());
//                    matrices.pop();
//                }
//
//                MatrixStack.Entry peek = matrices.peek();
//
//                matrices.pop();
//            }
//        }
    }

    private void renderItemStack(World world, ItemRenderer itemRenderer, ItemStack stack, double x, double y, double z, float scale, float ticks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(scale, scale, scale);
//            matrices.translate(0.82F, 0.68F, 0.82F);
        matrices.translate(x * (1.7D + scale), y, z * (1.7D + scale));

//        float l = MathHelper.sin(ticks / 3.1415927F) / (4.0F + ticks / 3.0F);
//        matrices.translate(0,  (((ticks > 180 ? (360 - ticks) : ticks) / 180.0F) * 0.39F), 0);
//        matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion((float) ticks));
        if (stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock().getDefaultState().isSolidBlock(world, BlockPos.ORIGIN)) {
//            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(ticks));
        } else {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
//            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(ticks));
        }

        itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, stack.hashCode());
        matrices.pop();
    }
}
