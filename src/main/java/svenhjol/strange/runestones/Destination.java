package svenhjol.strange.runestones;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.outerlands.module.Outerlands;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.runestones.tileentity.RunestoneTileEntity;

import java.util.Random;

public class Destination {
    public ResourceLocation structure;
    public float weight;

    public Destination(ResourceLocation structure, float weight) {
        this.structure = structure;
        this.weight = weight;
    }

    public Destination(float weight) {
        this(RunestoneHelper.SPAWN, weight);
    }

    public boolean isSpawnPoint() {
        return this.structure.equals(RunestoneHelper.SPAWN);
    }

    public BlockPos getDest(ServerWorld world, BlockPos runePos, Random rand) {
        // does the runestone have a destination already stored?
        TileEntity tile = world.getTileEntity(runePos);
        if (tile instanceof RunestoneTileEntity) {
            RunestoneTileEntity runestone = (RunestoneTileEntity)tile;
            String destination = runestone.destination;
            BlockPos position = runestone.position;

            if (destination != null && !destination.isEmpty()) {
                Strange.LOG.debug("Destination stored in runestone, using that instead of calculating new one.");
                return position;
            }
        }

        Strange.LOG.debug("Structure: " + structure);
        int maxDist = Runestones.maxDist;

        BlockPos spawn = world.getSpawnPoint();
        final int x = runePos.getX();
        final int z = runePos.getZ();
        final WorldBorder border = world.getWorldBorder();

        if (isSpawnPoint())
            return spawn;

        final int xdist = -maxDist + rand.nextInt(maxDist *2);
        final int zdist = -maxDist + rand.nextInt(maxDist *2);
        BlockPos p = runePos.add(xdist, 0, zdist);

        if (p.getX() > border.maxX())
            p = new BlockPos(border.maxX(), p.getY(), p.getZ());

        if (p.getX() < border.minX())
            p = new BlockPos(border.minX(), p.getY(), p.getZ());

        if (p.getZ() > border.maxZ())
            p = new BlockPos(p.getX(), p.getY(), border.maxZ());

        if (p.getZ() < border.minZ())
            p = new BlockPos(p.getX(), p.getY(), border.minZ());

        BlockPos target;
        BlockPos dest;

        if (Outerlands.isOuterPos(runePos)) {
            target = RunestoneHelper.normalizeOuterPos(p); // if you're in the outerlands, find a close-by outerlands pos
        } else {
            target = RunestoneHelper.normalizeInnerPos(p); // if you're not in outerlands, find a close-by inner pos
        }

        if (structure.equals(RunestoneHelper.SPAWN)) {
            dest = world.getSpawnPoint();
        } else {
            dest = world.findNearestStructure(structure.toString(), target, Runestones.maxDist, true);
            if (dest == null)
                dest = world.getSpawnPoint();
        }

        return dest;
    }
}
