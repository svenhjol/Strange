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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class StorageCrateBlockEntityRenderer<T extends StorageCrateBlockEntity> implements BlockEntityRenderer<T> {
    protected BlockEntityRendererFactory.Context context;

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

        int x = entity.getPos().getX();
        int y = entity.getPos().getY();
        int z = entity.getPos().getZ();

        BlockEntityRenderDispatcher dispatcher = this.context.getRenderDispatcher();
        Camera camera = dispatcher.camera;
        double d = camera.getPos().squaredDistanceTo(x, y, z);

        if (d < 32) {
            ItemStack stack = new ItemStack(item);
            int startLight = 0xFFFFFF;
            int maxLayers = 4;
            int itemsPerLayer = 6;
            int t = maxLayers * itemsPerLayer;
            boolean firstpass = count <= t;

            int maxCount = firstpass ? count : (t + 1 - itemsPerLayer) + ((count - 1) % itemsPerLayer);

            int layer = 0;
            int seed = 0;

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            Random random = new Random();

            for (int i = 0; i < t; i++) {
                if (i % itemsPerLayer == 0) {
                    layer = layer + 1;
                }

                seed = firstpass ? layer : layer + (int) Math.ceil(((double) (count - t) / itemsPerLayer));
                random.setSeed((long) entity.hashCode() * (seed + 1));

                int layerLight = (light >> (firstpass ? ((count - 1) / itemsPerLayer) / layer : maxLayers - layer)) - 1;

                matrices.push();
                matrices.translate(0.05F, 0.1D + (layer * 0.13D), 0.05F);

                int f = maxCount - i;
                int k = (firstpass || layer >= maxLayers - 2) ? 1 : 2;
                float s = 0.44F - (0.02F * (maxLayers - layer));

                for (int j = 0; j < Math.min(itemsPerLayer, f); j = j + k) {
                    matrices.push();
                    matrices.translate(0.15D + random.nextDouble() * 0.65D, 0.0D, 0.15D + random.nextDouble() * 0.65D);
                    matrices.scale(s, s, s);
                    matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-25 + (random.nextFloat() * 50)));
                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-180 + (random.nextFloat() * 360)));
                    itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, layerLight, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.hashCode());
                    matrices.pop();
                }

                matrices.pop();
            }
        }
    }
}
