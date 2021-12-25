package svenhjol.strange.module.teleport;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.teleport.iface.ITeleportType;
import svenhjol.strange.module.teleport.iface.ITicket;
import svenhjol.strange.module.teleport.runic.RunicTeleport;
import svenhjol.strange.module.teleport.ticket.RepositionTicket;
import svenhjol.strange.module.teleport.ticket.TeleportTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class Teleport extends CharmModule {
    private static final List<ITeleportType> TYPES = new ArrayList<>();
    private static final List<ITicket> TELEPORT_TICKETS = new ArrayList<>();
    private static final List<ITicket> REPOSITION_TICKETS = new ArrayList<>();

    public static List<UUID> noEndPlatform = new ArrayList<>();
    public static ThreadLocal<Entity> entityCreatingPlatform = new ThreadLocal<>();

    // TODO: configuration for ticks
    public static int teleportTicks = 10;
    public static int repositionTicks = 5;

    @Override
    public void register() {
        TYPES.add(new RunicTeleport());
        TYPES.forEach(ITeleportType::register);

        // Bound the ticks config. min = 0, max = 20
        teleportTicks = Mth.clamp(teleportTicks, 0, 20);
        repositionTicks = Mth.clamp(repositionTicks, 0, 20);
    }

    @Override
    public void runWhenEnabled() {
        ServerTickEvents.END_SERVER_TICK.register(this::handleTick);
        TYPES.forEach(ITeleportType::runWhenEnabled);
    }

    public static boolean hasTeleportTicket(Entity entity) {
        return TELEPORT_TICKETS.stream().anyMatch(t -> t.getEntity().getUUID() == entity.getUUID());
    }

    public static boolean hasRepositionTicket(Entity entity) {
        return REPOSITION_TICKETS.stream().anyMatch(t -> t.getEntity().getUUID() == entity.getUUID());
    }

    public static void addTeleportTicket(TeleportTicket ticket) {
        if (!hasTeleportTicket(ticket.getEntity())) {
            TELEPORT_TICKETS.add(ticket);
        }
    }

    public static void addRepositionTicket(RepositionTicket ticket) {
        if (!hasRepositionTicket(ticket.getEntity())) {
            REPOSITION_TICKETS.add(ticket);
        }
    }

    private void handleTick(MinecraftServer server) {
        tick(TELEPORT_TICKETS);
        tick(REPOSITION_TICKETS);
    }

    private void tick(List<ITicket> tickets) {
        if (!tickets.isEmpty()) {
            List<ITicket> ticketsToRemove = tickets.stream().filter(entry -> !entry.isValid()).collect(Collectors.toList());
            tickets.forEach(entry -> {
                entry.tick();

                if (!entry.isValid()) {
                    entry.fail();
                    ticketsToRemove.add(entry);
                }

                if (entry.isSuccessful()) {
                    entry.success();
                    ticketsToRemove.add(entry);
                }
            });

            if (!ticketsToRemove.isEmpty()) {
                int size = ticketsToRemove.size();

                ticketsToRemove.stream().findFirst().ifPresent(first
                    -> LogHelper.debug(Strange.MOD_ID, this.getClass(), "Removing " + size + " tickets of class " + first.getClass().getSimpleName()));

                ticketsToRemove.forEach(tickets::remove);
            }
        }
    }

}
