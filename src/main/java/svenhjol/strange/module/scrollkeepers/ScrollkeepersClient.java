package svenhjol.strange.module.scrollkeepers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.scrollkeepers.network.ClientReceiveSatisfied;
import svenhjol.strange.module.scrollkeepers.network.ClientSendCheckSatisfied;

import java.util.List;

@ClientModule(module = Scrollkeepers.class)
public class ScrollkeepersClient extends CharmModule {
    public static boolean hasSatisfiedQuest;
    private int backoff = 1; // exponential backoff when checking for scrollkeepers nearby

    public static ClientReceiveSatisfied CLIENT_RECEIVE_SATISFIED;
    public static ClientSendCheckSatisfied CLIENT_SEND_CHECK_SATISFIED;

    @Override
    public void runWhenEnabled() {
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        CLIENT_RECEIVE_SATISFIED = new ClientReceiveSatisfied();
        CLIENT_SEND_CHECK_SATISFIED = new ClientSendCheckSatisfied();
    }

    private void handlePlayerTick(Player player) {
        if (!player.level.isClientSide || player.level.getGameTime() % (30L * backoff) > 0) return;
        Level level = player.level;
        BlockPos pos = player.blockPosition();

        int range = 16;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // get villagers in range
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, new AABB(
            x - range, y - range, z - range, x + range, y + range, z + range
        ));
        if (villagers.isEmpty()) {
            if (backoff < 4) ++backoff;
            return;
        }
        backoff = 1;
        for (Villager villager : villagers) {
            if (villager.getVillagerData().getProfession() == Scrollkeepers.SCROLLKEEPER) {
                CLIENT_SEND_CHECK_SATISFIED.send();
                if (hasSatisfiedQuest) {
                    showParticlesInterest(villager);
                }
            }
        }
    }

    private void showParticlesInterest(Villager villager) {
        double spread = 0.75D;
        for (int i = 0; i < 3; i++) {
            double px = villager.blockPosition().getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = villager.blockPosition().getY() + 2.25D + (Math.random() - 0.5D) * spread;
            double pz = villager.blockPosition().getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            villager.level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0, 0.12D);
        }
    }
}
