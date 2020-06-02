package svenhjol.strange.base.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.Meson;
import svenhjol.strange.runestones.module.Runestones;
import svenhjol.strange.stonecircles.module.StoneCircles;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;

public class ItemHelper {
    public static ItemStack getDistantTotem(World world) {
        ItemStack out = ItemStack.EMPTY;
        if (Meson.isModuleEnabled("strange:stone_circles")
            && Meson.isModuleEnabled("strange:totem_of_returning")
            && world instanceof ServerWorld
        ) {
            ServerWorld serverWorld = (ServerWorld)world;
            out = new ItemStack(TotemOfReturning.item);
            BlockPos pos = serverWorld.findNearestStructure(StoneCircles.RESNAME, Runestones.getOuterPos(serverWorld, serverWorld.rand), 1000, true);
            if (pos == null) return null;

            TotemOfReturningItem.setPos(out, pos.add(0, serverWorld.getSeaLevel(), 0));
            TotemOfReturningItem.setGenerated(out, true);
            out.setDisplayName(new TranslationTextComponent("item.strange.quest_reward_totem"));
        }
        return out;
    }
}
