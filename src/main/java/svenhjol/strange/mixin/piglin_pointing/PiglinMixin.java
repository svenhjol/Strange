package svenhjol.strange.mixin.piglin_pointing;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.strange.feature.piglin_pointing.PiglinPointing;

@Mixin(Piglin.class)
public abstract class PiglinMixin extends AbstractPiglin {
    public PiglinMixin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "defineSynchedData",
        at = @At("TAIL")
    )
    private void hookDefineSynchedData(CallbackInfo ci) {
        this.entityData.define(PiglinPointing.entityDataIsPointing, false);
    }
}
