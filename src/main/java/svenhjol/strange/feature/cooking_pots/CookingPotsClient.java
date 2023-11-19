package svenhjol.strange.feature.cooking_pots;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.cooking_pots.CookingPotsNetwork.AddedToCookingPot;

import java.util.List;

public class CookingPotsClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return CookingPots.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();

        registry.blockColor(this::handleBlockColor, List.of(CookingPots.block));
        registry.itemTab(CookingPots.block, CreativeModeTabs.FUNCTIONAL_BLOCKS, Items.CAULDRON);
        registry.itemTab(CookingPots.mixedStewItem, CreativeModeTabs.FOOD_AND_DRINKS, Items.RABBIT_STEW);
    }

    private int handleBlockColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            return switch (state.getValue(CookingPotBlock.COOKING_STATUS)) {
                case COOKED -> 0x502800;
                case IN_PROGRESS -> 0x806030;
                case NONE -> 0x0088cc;
            };
        }
        return -1;
    }

    public static void handleAddedToCookingPot(AddedToCookingPot message, Player player) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;

        if (level != null) {
            var pos = message.getPos();
            var state = level.getBlockState(pos);
            createParticles(level, pos);
            level.updateNeighborsAt(pos, state.getBlock());
            level.setBlocksDirty(pos, state, state);
            level.sendBlockUpdated(pos, state, state, 2);
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
