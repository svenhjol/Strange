package svenhjol.strange.feature.chairs;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import svenhjol.charm_api.event.BlockUseEvent;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.strange.Strange;

import java.util.function.Supplier;

/**
 * Inspired by Quark's SitInStairs module.
 */
@Feature(mod = Strange.MOD_ID, description = "Right-click (with empty hand) on any stairs block to sit down.")
public class Chairs extends CharmFeature {
    static Supplier<EntityType<ChairEntity>> ENTITY;

    @Override
    public void register() {
        ENTITY = Strange.REGISTRY.entity("chair", () -> EntityType.Builder
            .<ChairEntity>of(ChairEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(1)
            .updateInterval(1));
    }

    @Override
    public void runWhenEnabled() {
        BlockUseEvent.INSTANCE.handle(this::handleBlockUse);
    }

    private InteractionResult handleBlockUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()
            && player.getMainHandItem().isEmpty()
            && !player.isPassenger()
            && !player.isCrouching()
        ) {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            var stateAbove = level.getBlockState(pos.above());
            var block = state.getBlock();

            if (block instanceof StairBlock
                && state.getValue(StairBlock.HALF) == Half.BOTTOM
                && !stateAbove.isCollisionShapeFullBlock(level, pos.above())
            ) {
                var chair = new ChairEntity(level, pos);
                level.addFreshEntity(chair);
                Strange.LOG.debug(getClass(), "Added new chair entity");

                var result = player.startRiding(chair);
                Strange.LOG.debug(getClass(), "Player is now riding");
                if (result) {
                    player.moveTo(chair.getX(), chair.getY(), chair.getZ());
                    player.setPos(chair.getX(), chair.getY(), chair.getZ());
                    Strange.LOG.debug(getClass(), "Moved player to chair pos");
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
