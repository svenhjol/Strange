package svenhjol.strange.module.teleport.iface;

import net.minecraft.world.entity.LivingEntity;

public interface ITicket {
    void tick();

    boolean isValid();

    boolean isSuccessful();

    default void success() {}

    default void fail() {}

    LivingEntity getEntity();
}
