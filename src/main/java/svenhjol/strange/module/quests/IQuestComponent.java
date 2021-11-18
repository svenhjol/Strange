package svenhjol.strange.module.quests;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import svenhjol.strange.iface.ISerializable;

import javax.annotation.Nullable;

public interface IQuestComponent extends ISerializable {
    String getId();

    default void abandon(Player player) {
        // no op
    }

    default void complete(Player player, @Nullable AbstractVillager merchant) {
        // no op
    }

    default void entityKilled(LivingEntity entity, Entity attacker) {
        // no op
    }

    default boolean isSatisfied(Player player) {
        return true;
    }

    default void playerTick(Player player) {
        // no op
    }

    boolean start(Player player);

    void update(Player player);
}
