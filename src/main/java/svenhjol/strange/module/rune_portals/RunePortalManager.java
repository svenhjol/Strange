package svenhjol.strange.module.rune_portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.Charm;
import svenhjol.charm.mixin.accessor.ServerPlayerAccessor;
import svenhjol.strange.init.StrangeSounds;

import java.util.*;
import java.util.stream.Collectors;

public class RunePortalManager extends SavedData {
    public static final String RUNES_NBT = "Runes";

    private final Level world;
    private final Map<BlockPos, DyeColor> colors = new HashMap<>();
    private Map<String, List<BlockPos>> runes = new HashMap<>();

    public RunePortalManager(ServerLevel world) {
        this.world = world;
        setDirty();
    }
    
    public static RunePortalManager fromNbt(ServerLevel world, CompoundTag nbt) {
        RunePortalManager manager = new RunePortalManager(world);
        CompoundTag runes = (CompoundTag)nbt.get(RUNES_NBT);

        manager.runes = new HashMap<>();

        if (runes != null && !runes.isEmpty()) {
            runes.getAllKeys().forEach(s -> {
                List<BlockPos> value = Arrays.stream(runes.getLongArray(s)).mapToObj(BlockPos::of).collect(Collectors.toList());
                manager.runes.put(s, new ArrayList<>(value));
            });
        }

        return manager;
    }
    
    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag colorNbt = new CompoundTag();
        CompoundTag runesNbt = new CompoundTag();

        colors.forEach((source, dye) ->
            colorNbt.putInt(String.valueOf(source.asLong()), dye.getId()));

        runes.forEach((key, value) -> {
            runesNbt.putLongArray(key, value.stream().map(BlockPos::asLong).collect(Collectors.toList()));
        });

        nbt.put(RUNES_NBT, runesNbt);
        return nbt;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "strange_rune_portals" + dimensionType.getFileSuffix();
    }

    public void createPortal(List<Integer> runes, BlockPos pos, Axis orientation) {
        String runesString = runesToString(runes);
        String replaced = runesString.replace(",", "");
        int half = replaced.length()/2;
        String s1 = replaced.substring(0, half);
        String s2 = replaced.substring(half + 1);

        long seed = Long.parseLong(s2) - Long.parseLong(s1);
        Random rand = new Random(seed);
        DyeColor color = DyeColor.byId(rand.nextInt(16));

        if (color == DyeColor.BLACK) {
            color = DyeColor.GRAY; // TOOD: reserve black portals
        }

        createPortal(runesString, pos, orientation, color);
    }

    public void createPortal(String runes, BlockPos start, Axis orientation, DyeColor color) {
        if (!this.runes.containsKey(runes)) {
            this.runes.put(runes, new ArrayList<>(Arrays.asList(start)));
        } else {
            if (!this.runes.get(runes).contains(start))
                this.runes.get(runes).add(start);
        }

        this.setDirty();

        // break the portal and unset all block entities first
        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? start.offset(a, b, 0) : start.offset(0, b, a);
                world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                world.removeBlockEntity(p);
            }
        }

        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? start.offset(a, b, 0) : start.offset(0, b, a);

                world.setBlock(p, RunePortals.RUNE_PORTAL_BLOCK.defaultBlockState()
                    .setValue(RunePortalBlock.AXIS, orientation)
                    .setValue(RunePortalBlock.COLOR, color.getId()), 3);

                setPortalBlockEntity(world, p, runes, start, orientation, color);
            }
        }

        // TODO: better portal create sound
        world.playSound(null, start, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.05F, 0.75F);
    }

    public void removePortal(String runes, BlockPos pos) {
        if (this.runes.containsKey(runes)) {
            this.runes.get(runes).remove(pos);
            if (this.runes.get(runes).stream().distinct().count() == 0)
                this.runes.remove(runes);
        }
        this.colors.remove(pos);
        this.setDirty();
    }

    public boolean teleport(String runes, BlockPos pos, Entity entity) {
        if (!this.runes.containsKey(runes))
            return false;

        if (this.runes.get(runes).size() < 2)
            return false;

        List<BlockPos> dests = this.runes.get(runes);
        Collections.shuffle(dests);
        Optional<BlockPos> optional = dests.stream().filter(b -> b != pos).findFirst();

        dests.forEach(dest -> {
            Charm.LOG.info(dest.toShortString());
        });
        Charm.LOG.warn(pos.toShortString());

        if (optional.isPresent()) {
            BlockPos dest = optional.get();

            if (entity instanceof ServerPlayer)
                ((ServerPlayerAccessor)entity).setIsChangingDimension(true);

            entity.teleportToWithTicket(dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5);
            world.playSound(null, dest, StrangeSounds.RUNESTONE_TRAVEL, SoundSource.BLOCKS, 0.85F, 1.05F);

//            if (entity instanceof ServerPlayerEntity)
//                ((ServerPlayerEntity)entity).onTeleportationDone();

            return true;
        }

        return false;
    }

    private void setPortalBlockEntity(Level world, BlockPos pos, String runes, BlockPos start, Axis orientation, DyeColor color) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
        portal.orientation = orientation;
        portal.pos = start;
        portal.runes = runes;
        portal.setChanged();
        portal.sync();
    }

    private static String runesToString(List<Integer> runes) {
        return runes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
