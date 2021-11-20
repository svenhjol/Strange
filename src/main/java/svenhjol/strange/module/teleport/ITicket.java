package svenhjol.strange.module.teleport;

import net.minecraft.world.entity.LivingEntity;

public interface ITicket {
    void tick();

    boolean isValid();

    boolean isSuccess();

    void onSuccess();

    void onFail();

    LivingEntity getEntity();
}
