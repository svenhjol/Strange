package svenhjol.strange.runeportals;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.*;
import java.util.stream.Collectors;

public class RunePortalManager extends PersistentState {
    public static final String COLORS_NBT = "Colors";
    public static final String RUNES_NBT = "Runes";
    
    private final World world;
    private Map<BlockPos, DyeColor> colors = new HashMap<>();
    private Map<List<Integer>, List<BlockPos>> runes = new HashMap<>();

    public RunePortalManager(ServerWorld world) {
        this.world = world;
        markDirty();
    }
    
    public static RunePortalManager fromNbt(ServerWorld world, NbtCompound nbt) {
        RunePortalManager manager = new RunePortalManager(world);
        NbtCompound colors = (NbtCompound)nbt.get(COLORS_NBT);
        NbtCompound runes = (NbtCompound)nbt.get(RUNES_NBT);

        manager.colors = new HashMap<>();
        manager.runes = new HashMap<>();

        if (colors != null && !colors.isEmpty()) {
            colors.getKeys().forEach(s -> {
                BlockPos source = BlockPos.fromLong(Long.parseLong(s));
                DyeColor color = DyeColor.byId(colors.getInt(s));
                manager.colors.put(source, color);
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

        colors.forEach((source, dye) ->
            colorNbt.putInt(String.valueOf(source.asLong()), dye.getId()));

        runes.forEach((key, value) -> {
            String runeKey = key.stream().map(String::valueOf).collect(Collectors.joining(","));
            runesNbt.putLongArray(runeKey, value.stream().map(BlockPos::asLong).collect(Collectors.toList()));
        });

        nbt.put(COLORS_NBT, colorNbt);
        nbt.put(RUNES_NBT, runesNbt);
        return nbt;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "rune_portals" + dimensionType.getSuffix();
    }

}
