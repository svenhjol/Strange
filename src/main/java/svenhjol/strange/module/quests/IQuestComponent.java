package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public interface IQuestComponent {
    String getId();

    boolean isPresent();

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

    default void provideMap(ServerPlayer player) {
        // no op
    }

    boolean start(Player player);

    void update(Player player);

    CompoundTag save();

    void load(CompoundTag tag);
}
