package svenhjol.strange.mixin.villager_tweaks;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import svenhjol.strange.feature.villager_tweaks.VillagerTweaks;

@Mixin(Villager.class)
public class VillagerMixin {
    @ModifyArg(
        method = "onReputationEventFrom",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/gossip/GossipContainer;add(Ljava/util/UUID;Lnet/minecraft/world/entity/ai/gossip/GossipType;I)V",
            ordinal = 0
        ),
        index = 2
    )
    private int hookMajorPositiveGossip(int val) {
        return VillagerTweaks.shouldRemoveZombieDiscount() ? 0 : val;
    }

    @ModifyArg(
        method = "onReputationEventFrom",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/gossip/GossipContainer;add(Ljava/util/UUID;Lnet/minecraft/world/entity/ai/gossip/GossipType;I)V",
            ordinal = 1
        ),
        index = 2
    )
    private int hookMinorPositiveGossip(int val) {
        return VillagerTweaks.shouldRemoveZombieDiscount() ? 0 : val;
    }
}