package svenhjol.strange.feature.casks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;

public class CasksClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Casks.class;
    }

    @Override
    public void runWhenEnabled() {
        var registry = mod().registry();
        registry.itemTab(Casks.block, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.JUKEBOX);
    }

    public static void handleAddedToCask(CasksNetwork.AddedToCask message, Player player) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.level != null) {
            createParticles(minecraft.level, message.getPos());
        }
    }

    public static void createParticles(Level level, BlockPos pos) {
        var random = level.getRandom();
        for(int i = 0; i < 10; ++i) {
            var offsetX = random.nextGaussian() * 0.02d;
            var offsetY = random.nextGaussian() * 0.02d;
            var offsetZ = random.nextGaussian() * 0.02d;

            level.addParticle(ParticleTypes.SMOKE,
                pos.getX() + 0.13 + (0.73d * random.nextFloat()),
                pos.getY() + 0.8d + random.nextFloat() * 0.3d,
                pos.getZ() + 0.13d + (0.73d * random.nextFloat()),
                offsetX, offsetY, offsetZ);
        }
    }
}
