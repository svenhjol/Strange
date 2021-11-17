package svenhjol.strange.module.quests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import svenhjol.strange.iface.ISerializable;

import javax.annotation.Nullable;

public interface IQuestComponent extends ISerializable {
    String getId();

    default void abandon(ServerPlayer player) {
        // no op
    }

    default void complete(ServerPlayer player, @Nullable AbstractVillager merchant) {
        // no op
    }

    default void entityKilled(LivingEntity entity, Entity attacker) {
        // no op
    }

    default boolean isSatisfied(ServerPlayer player) {
        return true;
    }

    default void playerTick(ServerPlayer player) {
        // no op
    }

    boolean start(ServerPlayer player);

    void update(ServerPlayer player);
}
