package svenhjol.strange.base.helper;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

import javax.annotation.Nullable;

public class LocationHelper {
    @Nullable
    public static ItemStack createMap(ServerWorld world, BlockPos pos, ResourceLocation res) {
        ItemStack map = null;

        BlockPos structurePos = world.findNearestStructure(res.toString(), pos, 100, true);
        if (structurePos != null) {
            map = FilledMapItem.setupNewMap(world, structurePos.getX(), structurePos.getZ(), (byte) 2, true, true);
            FilledMapItem.renderBiomePreviewMap(world, map);
            MapData.addTargetDecoration(map, structurePos, "+", MapDecoration.Type.TARGET_X);
            map.setDisplayName(new TranslationTextComponent("filled_map." + res.getPath()));
        }

        return map;
    }
}
