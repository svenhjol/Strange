package svenhjol.strange.feature.runestones.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.charmony.feature.FeatureHolder;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.runestones.common.Networking;

@SuppressWarnings("unused")
public final class Handlers extends FeatureHolder<RunestonesClient> {
    public Handlers(RunestonesClient feature) {
        super(feature);
    }

    public void sacrificePositionReceived(Player player, Networking.S2CSacrificeInProgress packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var random = level.getRandom();
        var itemParticle = ParticleTypes.SMOKE;
        var runestoneParticle = ParticleTypes.ENCHANT;

        var itemDist = 0.1d;
        var itemPos = packet.itemPos();

        var runestoneDist = 3.0d;
        var runestonePos = packet.runestonePos();

        for (var i = 0; i < 8; i++) {
            level.addParticle(itemParticle, itemPos.x(), itemPos.y() + 0.36d, itemPos.z(),
                (itemDist / 2) - (random.nextDouble() * itemDist), 0, (itemDist / 2) - (random.nextDouble() * itemDist));
        }

        for (var i = 0; i < 8; i++) {
            level.addParticle(runestoneParticle, runestonePos.getX() + 0.5d, runestonePos.getY() + 0.68d, runestonePos.getZ() + 0.5d,
                (runestoneDist / 2) - (random.nextDouble() * runestoneDist), random.nextDouble(), (runestoneDist / 2) - (random.nextDouble() * runestoneDist));
        }
    }

    public void activateRunestoneReceived(Player player, Networking.S2CActivateRunestone packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var random = level.getRandom();
        var particle = ParticleTypes.LARGE_SMOKE;
        var pos = packet.pos();
        var range = 1.3d;

        for (var i = 0; i < 80; i++) {
            level.addParticle(particle,
                pos.getX() + 0.5d + ((random.nextDouble() * range) - (random.nextDouble() * range)),
                pos.getY() + 0.5d + ((random.nextDouble() * (range / 2)) - (random.nextDouble() * (range / 2))),
                pos.getZ() + 0.5d + ((random.nextDouble() * range) - (random.nextDouble() * range)), 0, 0, 0);
        }
    }
}
