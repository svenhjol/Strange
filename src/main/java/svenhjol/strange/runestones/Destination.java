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

import java.util.HashMap;
import java.util.Map;
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

    public BlockPos getAndRecordDestination(ServerWorld world, BlockPos runePos, Random rand) {
        // does the runestone have a destination already stored?
        TileEntity tile = world.getTileEntity(runePos);
        if (tile instanceof RunestoneTileEntity) {
            RunestoneTileEntity runestone = (RunestoneTileEntity)tile;
            String destination = runestone.destination;
            BlockPos position = runestone.position;

            if (destination != null && !destination.isEmpty()) {
                Strange.LOG.debug("Found destination in the runestone: using that instead of calculating new one.");
                return position;
            }
        }

        Strange.LOG.debug("Structure: " + structure);
        int maxDist = Runestones.maxDist;
        final WorldBorder border = world.getWorldBorder();

        final int xdist = -maxDist + rand.nextInt(maxDist *2);
        final int zdist = -maxDist + rand.nextInt(maxDist *2);
        BlockPos p = runePos.add(xdist, 0, zdist);

        // bounds check
        if (p.getX() > border.maxX())
            p = new BlockPos(border.maxX(), p.getY(), p.getZ());

        if (p.getX() < border.minX())
            p = new BlockPos(border.minX(), p.getY(), p.getZ());

        if (p.getZ() > border.maxZ())
            p = new BlockPos(p.getX(), p.getY(), border.maxZ());

        if (p.getZ() < border.minZ())
            p = new BlockPos(p.getX(), p.getY(), border.minZ());

        BlockPos currentLocation;
        BlockPos foundPos;

        if (Outerlands.isOuterPos(runePos)) {
            currentLocation = RunestoneHelper.normalizeOuterPos(p); // if you're in the outerlands, find a close-by outerlands pos
        } else {
            currentLocation = RunestoneHelper.normalizeInnerPos(p); // if you're not in outerlands, find a close-by inner pos
        }

        if (isSpawnPoint()) {
            foundPos = world.getSpawnPoint();
        } else {
            String structureName = getStructureName();
            foundPos = world.findNearestStructure(structureName, currentLocation, Runestones.maxDist, true);
            if (foundPos == null) {
                Strange.LOG.warn("World could not locate structure " + structureName + ", defaulting to spawn position.");
                foundPos = world.getSpawnPoint();
            }
        }

        foundPos = RunestoneHelper.addRandomOffset(foundPos, rand, 8);

        // record the destination and position in the runestone
        if (tile instanceof RunestoneTileEntity) {
            RunestoneTileEntity runestone = (RunestoneTileEntity)tile;
            runestone.position = foundPos;
            runestone.destination = structure.toString();
            runestone.markDirty();

            Strange.LOG.debug("Stored destination in runestone for next time.");
        }

        return foundPos;
    }

    private String getStructureName() {
        String structureName;

        Map<String, String> vanillaMap = new HashMap<>();
        vanillaMap.put("buried_treasure", "Buried_Treasure");
        vanillaMap.put("desert_pyramid", "Desert_Pyramid");
        vanillaMap.put("endcity", "EndCity");
        vanillaMap.put("fortress", "Fortress");
        vanillaMap.put("igloo", "Igloo");
        vanillaMap.put("jungle_pyramid", "Jungle_Pyramid");
        vanillaMap.put("mansion", "Mansion");
        vanillaMap.put("mineshaft", "Mineshaft");
        vanillaMap.put("monument", "Monument");
        vanillaMap.put("ocean_ruin", "Ocean_Ruin");
        vanillaMap.put("pillager_outpost", "Pillager_Outpost");
        vanillaMap.put("shipwreck", "Shipwreck");
        vanillaMap.put("stronghold", "Stronghold");
        vanillaMap.put("swamp_hut", "Swamp_Hut");
        vanillaMap.put("village", "Village");

        if (structure.getNamespace().equals("minecraft")) {
            structureName = structure.getPath();
            if (vanillaMap.containsKey(structureName)) {
                structureName = vanillaMap.get(structureName);
            }
        } else {
            structureName = structure.toString();
        }

        return structureName;
    }
}
