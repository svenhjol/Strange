package svenhjol.strange.runeportals;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
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
    private static final int TELEPORT_TICKS = 180;

    public static final String COLORS_NBT = "Colors";
    public static final String RUNES_NBT = "Runes";

    private final World world;
    private Map<BlockPos, DyeColor> colors = new HashMap<>();
    private Map<String, List<BlockPos>> runes = new HashMap<>();

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
                List<BlockPos> value = Arrays.stream(runes.getLongArray(s)).mapToObj(BlockPos::fromLong).collect(Collectors.toList());
                manager.runes.put(s, new ArrayList<>(value));
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
            runesNbt.putLongArray(key, value.stream().map(BlockPos::asLong).collect(Collectors.toList()));
        });

        nbt.put(COLORS_NBT, colorNbt);
        nbt.put(RUNES_NBT, runesNbt);
        return nbt;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "rune_portals" + dimensionType.getSuffix();
    }

    public void createPortal(List<Integer> runes, BlockPos pos, Axis orientation, DyeColor color) {
        String runesString = runesToString(runes);

        if (!this.runes.containsKey(runesString)) {
            this.runes.put(runesString, new ArrayList<>(Arrays.asList(pos)));
        } else {
            this.runes.get(runesString).add(pos);
        }

        this.colors.put(pos, color);
        this.markDirty();

        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);
                world.setBlockState(p, RunePortals.RUNE_PORTAL_BLOCK.getDefaultState()
                    .with(RunePortalBlock.AXIS, orientation), 18);

                setPortal(world, p, runesString, pos, orientation, color);
            }
        }
    }

    public void removePortal(String runes, BlockPos pos) {
        if (this.runes.containsKey(runes)) {
            this.runes.get(runes).remove(pos);
            if (this.runes.get(runes).isEmpty()) {
                this.runes.remove(runes);
            }
        }
        this.colors.remove(pos);
        this.markDirty();
    }

    public boolean teleport(String runes, BlockPos pos, Entity entity) {
        if (!this.runes.containsKey(runes))
            return false;

        if (this.runes.get(runes).size() < 2)
            return false;

        List<BlockPos> dests = this.runes.get(runes);
        Collections.shuffle(dests);
        Optional<BlockPos> optional = dests.stream().filter(b -> b != pos).findFirst();
        if (optional.isPresent()) {
            BlockPos dest = optional.get();
            entity.requestTeleport(dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5);
            return true;
        }

        return false;
    }

    private void setPortal(World world, BlockPos pos, String runes, BlockPos start, Axis orientation, DyeColor color) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
        portal.orientation = orientation;
        portal.color = color;
        portal.pos = start;
        portal.runes = runes;
        portal.markDirty();

        world.playSound(null, pos, SoundEvents.ENTITY_MULE_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.getBlockTickScheduler().schedule(pos, RunePortals.RUNE_PORTAL_BLOCK, 2);
    }

    private static String runesToString(List<Integer> runes) {
        return runes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
