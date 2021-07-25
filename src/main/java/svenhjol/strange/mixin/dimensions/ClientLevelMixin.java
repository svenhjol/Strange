package svenhjol.strange.mixin.dimensions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
//    private final AmbientParticleSettings ASH = new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F);
//    private final AmbientParticleSettings SPORE = new AmbientParticleSettings(ParticleTypes.SPORE_BLOSSOM_AIR, 0.118093334F);
//    private final int SKY_COLOR = calculateSkyColor(2.0F);
//
//    @Inject(
//        method = "animateTick",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/multiplayer/ClientLevel;doAnimateTick(IIIILjava/util/Random;Lnet/minecraft/client/multiplayer/ClientLevel$MarkerParticleStatus;Lnet/minecraft/core/BlockPos$MutableBlockPos;)V",
//            ordinal = 0
//        )
//    )
//    private void animateTick(int i, int j, int k, CallbackInfo ci) {
//        ClientLevel level = (ClientLevel) (Object) this;
//
//        if (level.dimension().location().equals(Dimensions.DARKLAND_ID) && ASH.canSpawn(level.random)) {
//            int l = 24;
//            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
//            BlockState state = level.getBlockState(pos);
//
//            int x = i + level.random.nextInt(l) - level.random.nextInt(l);
//            int y = j + level.random.nextInt(l) - level.random.nextInt(l);
//            int z = k + level.random.nextInt(l) - level.random.nextInt(l);
//            pos.set(x, y, z);
//
//            if (!state.isCollisionShapeFullBlock(level, pos)) {
//                level.addParticle(ASH.getOptions(), (double) pos.getX() + level.random.nextDouble(), (double) pos.getY() + level.random.nextDouble(), (double) pos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
//                level.addParticle(SPORE.getOptions(), (double) pos.getX() + level.random.nextDouble(), (double) pos.getY() + level.random.nextDouble(), (double) pos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
//            }
//        }
//    }

//    @Inject(
//        method = "getSkyColor",
//        at = @At("HEAD"),
//        cancellable = true
//    )
//    private void hookGetSkyColor(Vec3 vec3, float f, CallbackInfoReturnable<Vec3> cir) {
//        ClientLevel level = (ClientLevel) (Object) this;
//
//        if (level.dimension().location().equals(Journals.DARKLAND_ID)) {
//            cir.setReturnValue(Vec3.fromRGB24(SKY_COLOR));
//        }
//    }
//
//    @Inject(
//        method = "getSkyDarken",
//        at = @At("HEAD"),
//        cancellable = true
//    )
//    private void hookGetSkyDarken(float f, CallbackInfoReturnable<Float> cir) {
//        ClientLevel level = (ClientLevel) (Object) this;
//
//        if (level.dimension().location().equals(Journals.DARKLAND_ID)) {
//            cir.cancel();
//        }
//    }

    private static int calculateSkyColor(float f) {
        float g = f / 3.0F;
        g = Mth.clamp(g, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - g * 0.05F, 0.5F + g * 0.1F, 1.0F);
    }
}
