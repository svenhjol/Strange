package svenhjol.strange.module.cooking_pots;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

@ClientModule(module = CookingPots.class)
public class CookingPotsClient extends CharmModule {
    private final Map<List<ResourceLocation>, List<Item>> cachedItems = new WeakHashMap<>();

    @Override
    public void register() {
        ColorProviderRegistry.BLOCK.register(this::handleColorProvider, CookingPots.COOKING_POT);
        BlockEntityRendererRegistry.register(CookingPots.BLOCK_ENTITY, CookingPotBlockEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(CookingPots.MSG_CLIENT_ADDED_TO_POT, this::handleClientAddedToPot);
    }

    private int handleColorProvider(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 1) {
            if (level != null) {
                if (state.getBlock() == CookingPots.COOKING_POT && state.getValue(CookingPotBlock.LIQUID) == 2) {
                    return 0x602A00;
                }
            }
            return 0x0088CC;
        }
        return -1;
    }

    private void handleClientAddedToPot(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        BlockPos pos = BlockPos.of(data.readLong());
        client.execute(() -> {
            if (client.level != null) {
                createParticles(client.level, pos);
            }
        });
    }

    private void createParticles(Level level, BlockPos pos) {
        Random random = level.getRandom();

        for(int i = 0; i < 10; ++i) {
            double g = random.nextGaussian() * 0.02D;
            double h = random.nextGaussian() * 0.02D;
            double j = random.nextGaussian() * 0.02D;
            level.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.13 + (0.73D * (double)random.nextFloat()), (double)pos.getY() + 0.8D + (double)random.nextFloat() * 0.3D, (double)pos.getZ() + 0.13D + (0.73 * (double)random.nextFloat()), g, h, j);
        }
    }
}
