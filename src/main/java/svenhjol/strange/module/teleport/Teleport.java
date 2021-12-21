package svenhjol.strange.module.teleport;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeParticles;
import svenhjol.strange.module.teleport.runic.RunicTeleport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class Teleport extends CharmModule {
    public static final int TELEPORT_TICKS = 10;
    public static final int REPOSITION_TICKS = 5;

    public static final ResourceLocation MSG_CLIENT_TELEPORT_EFFECT = new ResourceLocation(Strange.MOD_ID, "client_teleport_effect");

    public static List<ITicket> teleportTickets = new ArrayList<>();
    public static List<ITicket> repositionTickets = new ArrayList<>();
    public static List<UUID> noEndPlatform = new ArrayList<>();
    public static ThreadLocal<Entity> entityCreatingPlatform = new ThreadLocal<>();

    public static final List<ITeleportType> TYPES = new ArrayList<>();

    @Override
    public void register() {
        TYPES.add(new RunicTeleport());

        TYPES.forEach(ITeleportType::register);
    }

    @Override
    public void runWhenEnabled() {
        ServerTickEvents.END_SERVER_TICK.register(this::handleTick);

        TYPES.forEach(ITeleportType::runWhenEnabled);
    }

    private void handleTick(MinecraftServer server) {
        tick(teleportTickets);
        tick(repositionTickets);
    }

    private void tick(List<ITicket> tickets) {
        if (!tickets.isEmpty()) {
            List<ITicket> toRemove = tickets.stream().filter(entry -> !entry.isValid()).collect(Collectors.toList());
            tickets.forEach(entry -> {
                entry.tick();

                if (!entry.isValid()) {
                    entry.onFail();
                    toRemove.add(entry);
                }

                if (entry.isSuccess()) {
                    entry.onSuccess();
                    toRemove.add(entry);
                }
            });

            if (!toRemove.isEmpty()) {
                int size = toRemove.size();
                toRemove.stream().findFirst().ifPresent(first -> LogHelper.debug(this.getClass(), "Removing " + size + " tickets of class " + first.getClass().getSimpleName()));
                toRemove.forEach(tickets::remove);
            }
        }
    }

    /**
     * Inform the client that we have started a teleport ticket. Allows for client effects.
     */
    public static void sendClientTeleportEffect(ServerPlayer player, BlockPos origin, Type type) {
        NetworkHelper.sendPacketToClient(player, MSG_CLIENT_TELEPORT_EFFECT, buf -> {
            buf.writeEnum(type);
            buf.writeBlockPos(origin);
        });
    }

    public enum Type {
        GENERIC(ParticleTypes.PORTAL),
        RUNESTONE(StrangeParticles.ILLAGERALT),
        RUNIC_TOME(StrangeParticles.ILLAGERALT),
        FAIL(ParticleTypes.SMOKE);

        private final ParticleOptions particle;

        Type(ParticleOptions particle) {
            this.particle = particle;
        }

        public ParticleOptions getParticle() {
            return particle;
        }
    }
}
