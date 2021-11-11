package svenhjol.strange.module.teleport;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.event.ActivateRunestoneCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Teleport extends CharmModule {
    public static final int TELEPORT_TICKS = 10;

    public static List<TeleportEntry> entries = new ArrayList<>();

    @Config(name = "Travel distance in blocks", description = "Maximum number of blocks that you will be teleported via a runestone.")
    public static int maxDistance = 5000;

    @Config(name = "Travel protection time", description = "Number of seconds of regeneration, fire resistance and slow fall after teleporting.")
    public static int protectionDuration = 10;

    @Config(name = "Travel penalty time", description = "Number of seconds of poison, wither, burning, slowness or weakness after teleporting without the correct item.")
    public static int penaltyDuration = 10;

    @Override
    public void runWhenEnabled() {
        ServerTickEvents.END_SERVER_TICK.register(this::handleTick);

        ActivateRunestoneCallback.EVENT.register(this::handleActivateRunestone);
    }

    private void handleActivateRunestone(Player player, String runes, ItemStack sacrifice) {
        LogHelper.debug(this.getClass(), "Teleport:", player, runes, sacrifice);
    }

    private void handleTick(MinecraftServer server) {
        if (!entries.isEmpty()) {
            List<TeleportEntry> toRemove = entries.stream().filter(entry -> !entry.isValid()).collect(Collectors.toList());
            entries.forEach(entry -> {
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
                toRemove.forEach(e -> entries.remove(e));
            }
        }
    }

//    public boolean travel(String runes, ItemStack sacrifice, LivingEntity entity, @Nullable BlockPos origin) {
//        if (entity.level.isClientSide) return false;
//        ServerLevel level = (ServerLevel)entity.level;
//
//        try {
//            BlockPos target;
//            Discovery discovery = get(runes).orElseThrow();
//            ResourceLocation dimension = discovery.getDimension().orElseThrow();
//            if (!DimensionHelper.isDimension(level, dimension)) return false;
//
//            Optional<BlockPos> pos = discovery.getPos();
//            if (origin == null) {
//                origin = entity.blockPosition();
//            }
//
//            if (pos.isEmpty()) {
//                Random random = new Random(origin.asLong());
//                target = discovery.getBaseLocation().getTarget(level, origin, random);
//                discovery.setPos(target);
//            } else {
//                target = pos.get();
//            }
//
//            Teleport.entries.add(new TeleportEntry(entity, dimension, origin, target, false, false));
//            return true;
//
//        } catch (Exception e) {
//            LogHelper.warn(this.getClass(), e.getMessage());
//            return false;
//        }
//    }

//    public enum LocationType implements ICharmEnum {
//        BIOME(BiomeLocation.class),
//        STRUCTURE(StructureLocation.class);
//
//        private final Class<? extends BaseLocation> clazz;
//
//        LocationType(Class<? extends BaseLocation> clazz) {
//            this.clazz = clazz;
//        }
//
//        public BaseLocation getLocation(ResourceLocation location, float difficulty) {
//            try {
//                return this.clazz.getDeclaredConstructor(ResourceLocation.class, float.class).newInstance(location, difficulty);
//            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e ) {
//                throw new RuntimeException("Could not resolve location, giving up: ", e);
//            }
//        }
//
//        public static LocationType fromResourceLocation(ResourceLocation id) {
//            if (WorldHelper.isStructure(id)) {
//                return LocationType.STRUCTURE;
//            } else if (id.equals(RunestoneLocations.SPAWN)) {
//                return LocationType.STRUCTURE;
//            } else {
//                return LocationType.BIOME;
//            }
//        }
//    }
}
