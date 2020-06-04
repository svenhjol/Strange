package svenhjol.strange.base.helper;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;

public class LocationHelper {
    @Nullable
    public static ItemStack createMap(ServerWorld world, BlockPos pos, ResourceLocation res) {
        ItemStack map = null;

        BlockPos structurePos = world.findNearestStructure(res.toString(), pos, 100, true);
        if (structurePos != null) {
            map = FilledMapItem.setupNewMap(world, structurePos.getX(), structurePos.getZ(), (byte) 2, true, true);
            FilledMapItem.func_226642_a_(world, map);
            MapData.addTargetDecoration(map, structurePos, "+", MapDecoration.Type.TARGET_X);
            map.setDisplayName(new TranslationTextComponent("filled_map." + res.getPath()));
        }

        return map;
    }

    public static boolean addForcedChunk(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        // try a couple of times I guess?
        boolean result = false;
        for (int i = 0; i <= 2; i++) {
            result = world.forceChunk(chunkPos.getXStart(), chunkPos.getZStart(), true);
            if (result) break;
        }
        if (result)
            Strange.LOG.debug("Force loaded chunk " + chunkPos.toString());

        return result;
    }

    public static boolean removeForcedChunk(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        boolean result = world.forceChunk(chunkPos.getXStart(), chunkPos.getZStart(), false);
        if (!result) {
            Strange.LOG.error("Could not unload forced chunk - this is probably really bad.");
        } else {
            Strange.LOG.debug("Unloaded forced chunk " + chunkPos.toString());
        }
        return result;
    }
}
