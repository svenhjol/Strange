package svenhjol.strange.runeportals;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.*;
import java.util.stream.Collectors;

public class RunePortalManager extends PersistentState {
    private static final DyeColor DEFAULT_COLOR = DyeColor.CYAN;

    public static final String COLORS_NBT = "Colors";
    public static final String RUNES_NBT = "Runes";
    public static final String ORIENTATIONS_NBT = "Orientations";
    
    private final World world;
    private Map<BlockPos, DyeColor> colors = new HashMap<>();
    private Map<BlockPos, Axis> orientations = new HashMap<>();
    private Map<List<Integer>, List<BlockPos>> runes = new HashMap<>();

    public RunePortalManager(ServerWorld world) {
        this.world = world;
        markDirty();
    }
    
    public static RunePortalManager fromNbt(ServerWorld world, NbtCompound nbt) {
        RunePortalManager manager = new RunePortalManager(world);
        NbtCompound colors = (NbtCompound)nbt.get(COLORS_NBT);
        NbtCompound runes = (NbtCompound)nbt.get(RUNES_NBT);
        NbtCompound orientations = (NbtCompound)nbt.get(ORIENTATIONS_NBT);

        manager.colors = new HashMap<>();
        manager.runes = new HashMap<>();
        manager.orientations = new HashMap<>();

        if (colors != null && !colors.isEmpty()) {
            colors.getKeys().forEach(s -> {
                BlockPos source = BlockPos.fromLong(Long.parseLong(s));
                DyeColor color = DyeColor.byId(colors.getInt(s));
                manager.colors.put(source, color);
            });
        }

        if (orientations != null && !orientations.isEmpty()) {
            orientations.getKeys().forEach(s -> {
                BlockPos source = BlockPos.fromLong(Long.parseLong(s));
                Axis orientation = Axis.fromName(orientations.getString(s));
                manager.orientations.put(source, orientation);
            });
        }

        if (runes != null && !runes.isEmpty()) {
            runes.getKeys().forEach(s -> {
                List<Integer> key = Arrays.stream(s.split(",")).map(Integer::parseInt).collect(Collectors.toList());
                List<BlockPos> value = Arrays.stream(runes.getLongArray(s)).mapToObj(BlockPos::fromLong).collect(Collectors.toList());
                manager.runes.put(key, value);
            });
        }

        return manager;
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound colorNbt = new NbtCompound();
        NbtCompound runesNbt = new NbtCompound();
        NbtCompound orientationsNbt = new NbtCompound();

        colors.forEach((source, dye) ->
            colorNbt.putInt(String.valueOf(source.asLong()), dye.getId()));

        orientations.forEach((source, orientation) ->
            orientationsNbt.putString(String.valueOf(source.asLong()), orientation.asString()));

        runes.forEach((key, value) -> {
            String runeKey = key.stream().map(String::valueOf).collect(Collectors.joining(","));
            runesNbt.putLongArray(runeKey, value.stream().map(BlockPos::asLong).collect(Collectors.toList()));
        });

        nbt.put(COLORS_NBT, colorNbt);
        nbt.put(RUNES_NBT, runesNbt);
        nbt.put(ORIENTATIONS_NBT, orientationsNbt);
        return nbt;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "rune_portals" + dimensionType.getSuffix();
    }

    public void createPortal(List<Integer> runes, BlockPos pos, Axis orientation, DyeColor color) {
        if (!this.runes.containsKey(runes)) {
            this.runes.put(runes, Arrays.asList(pos));
        } else {
            this.runes.get(runes).add(pos);
        }

        this.colors.put(pos, color);
        this.orientations.put(pos, orientation);

        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);
                world.setBlockState(p, RunePortals.RUNE_PORTAL_BLOCK.getDefaultState()
                    .with(RunePortalBlock.AXIS, orientation), 18);

                setPortal(world, p, runes, pos, orientation, color);
            }
        }
    }

    public void removePortal(List<Integer> runes, BlockPos pos) {
        this.runes.remove(runes);
        this.orientations.remove(pos);
        this.colors.remove(pos);
    }

    private void setPortal(World world, BlockPos pos, List<Integer> runes, BlockPos start, Axis orientation, DyeColor color) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
        portal.orientation = orientation;
        portal.color = color;
        portal.pos = start;
        portal.runes = runes;
        portal.markDirty();

        world.playSound(null, pos, SoundEvents.ENTITY_MULE_ANGRY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.getBlockTickScheduler().schedule(pos, RunePortals.RUNE_PORTAL_BLOCK, 2);
    }
}
