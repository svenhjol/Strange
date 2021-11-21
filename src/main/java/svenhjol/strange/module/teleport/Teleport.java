package svenhjol.strange.module.teleport;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.branches.*;
import svenhjol.strange.module.runestones.event.ActivateRunestoneCallback;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.event.ActivateRunicTomeCallback;
import svenhjol.strange.module.teleport.handler.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Teleport extends CharmModule {
    public static final int TELEPORT_TICKS = 10;
    public static final int REPOSITION_TICKS = 5;

    public static List<ITicket> teleportTickets = new ArrayList<>();
    public static List<ITicket> repositionTickets = new ArrayList<>();
    public static List<UUID> noEndPlatform = new ArrayList<>();
    public static ThreadLocal<Entity> entityCreatingPlatform = new ThreadLocal<>();

    private static final Map<String, Class<? extends TeleportHandler<?>>> handlers = new HashMap<>();

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 5000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration, fire resistance and slow fall after teleporting.")
    public static int protectionDuration = 10;

    @Config(name = "Travel penalty time", description = "Number of seconds of poison, wither, burning, slowness or weakness after teleporting without the correct item.")
    public static int penaltyDuration = 10;

    @Override
    public void register() {
        handlers.put(DiscoveriesBranch.NAME, DiscoveryTeleportHandler.class);
        handlers.put(SpecialsBranch.NAME, SpecialTeleportHandler.class);
        handlers.put(BiomesBranch.NAME, BiomeTeleportHandler.class);
        handlers.put(StructuresBranch.NAME, StructureTeleportHandler.class);
        handlers.put(DimensionsBranch.NAME, DimensionTeleportHandler.class);
        handlers.put(BookmarksBranch.NAME, BookmarkTeleportHandler.class);
    }

    @Override
    public void runWhenEnabled() {
        ServerTickEvents.END_SERVER_TICK.register(this::handleTick);

        // register the actions that can teleport a player
        ActivateRunestoneCallback.EVENT.register(this::handleActivateRunestone);
        ActivateRunicTomeCallback.EVENT.register(this::handleActivateRunicTome);
    }

    private void handleActivateRunestone(ServerPlayer player, BlockPos origin, String runes, ItemStack sacrifice) {
        tryGetHandler(player, runes, sacrifice, origin).ifPresent(TeleportHandler::process);
    }

    private void handleActivateRunicTome(ServerPlayer player, BlockPos origin, ItemStack tome, ItemStack sacrifice) {
        String runes = RunicTomeItem.getRunes(tome);
        tryGetHandler(player, runes, sacrifice, origin).ifPresent(TeleportHandler::process);
    }

    private Optional<TeleportHandler<?>> tryGetHandler(ServerPlayer player, String runes, ItemStack sacrifice, BlockPos origin) {
        TeleportHandler<?> handler;
        ServerLevel level = (ServerLevel)player.level;
        KnowledgeBranch<?, ?> branch = KnowledgeBranch.getByStartRune(runes.charAt(0)).orElseThrow();

        Class<? extends TeleportHandler<?>> clazz = handlers.getOrDefault(branch.getBranchName(), null);
        if (clazz == null) {
            return Optional.empty();
        }

        try {
            Constructor<? extends TeleportHandler<?>> constructor = clazz.getDeclaredConstructor(KnowledgeBranch.class, ServerLevel.class, LivingEntity.class, ItemStack.class, String.class, BlockPos.class);
            handler = constructor.newInstance(branch, level, player, sacrifice, runes, origin);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LogHelper.warn(this.getClass(), "Could not instantiate teleport handler: " + e.getMessage());
            return Optional.empty();
        }

        return Optional.of(handler);
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
                toRemove.forEach(tickets::remove);
            }
        }
    }
}
