package svenhjol.strange.module.rune_portals;

import net.minecraft.block.Blocks;
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
import svenhjol.strange.init.StrangeSounds;

import java.util.*;
import java.util.stream.Collectors;

public class RunePortalManager extends PersistentState {
    public static final String RUNES_NBT = "Runes";

    private final World world;
    private final Map<BlockPos, DyeColor> colors = new HashMap<>();
    private Map<String, List<BlockPos>> runes = new HashMap<>();

    public RunePortalManager(ServerWorld world) {
        this.world = world;
        markDirty();
    }
    
    public static RunePortalManager fromNbt(ServerWorld world, NbtCompound nbt) {
        RunePortalManager manager = new RunePortalManager(world);
        NbtCompound runes = (NbtCompound)nbt.get(RUNES_NBT);

        manager.runes = new HashMap<>();

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

        nbt.put(RUNES_NBT, runesNbt);
        return nbt;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "rune_portals" + dimensionType.getSuffix();
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

    public void createPortal(String runes, BlockPos pos, Axis orientation, DyeColor color) {
        if (!this.runes.containsKey(runes)) {
            this.runes.put(runes, new ArrayList<>(Arrays.asList(pos)));
        } else {
            if (!this.runes.get(runes).contains(pos))
                this.runes.get(runes).add(pos);
        }

        this.markDirty();

        // break the portal and unset all block entities first
        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);
                world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                world.removeBlockEntity(p);
            }
        }

        for (int a = -1; a < 2; a++) {
            for (int b = 1; b < 4; b++) {
                BlockPos p = orientation == Axis.X ? pos.add(a, b, 0) : pos.add(0, b, a);

                world.setBlockState(p, RunePortals.RUNE_PORTAL_BLOCK.getDefaultState()
                    .with(RunePortalBlock.AXIS, orientation)
                    .with(RunePortalBlock.COLOR, color.getId()), 3);

                setPortalBlockEntity(world, p, runes, pos, orientation, color);
            }
        }

        // TODO: better portal create sound
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 1.05F, 0.75F);
    }

    public void removePortal(String runes, BlockPos pos) {
        if (this.runes.containsKey(runes)) {
            this.runes.get(runes).remove(pos);
            if (this.runes.get(runes).stream().distinct().count() == 0)
                this.runes.remove(runes);
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
            world.playSound(null, dest, StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.BLOCKS, 0.85F, 1.05F);
            return true;
        }

        return false;
    }

    private void setPortalBlockEntity(World world, BlockPos pos, String runes, BlockPos start, Axis orientation, DyeColor color) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        RunePortalBlockEntity portal = (RunePortalBlockEntity)blockEntity;
        portal.orientation = orientation;
        portal.pos = start;
        portal.runes = runes;
        portal.markDirty();
        portal.sync();
    }

    private static String runesToString(List<Integer> runes) {
        return runes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
