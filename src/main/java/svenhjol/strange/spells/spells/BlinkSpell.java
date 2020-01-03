package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import svenhjol.meson.helper.WorldHelper;

import java.util.function.Consumer;

public class BlinkSpell extends Spell
{
    public BlinkSpell()
    {
        super("blink");
        this.color = DyeColor.MAGENTA;
        this.affect = Affect.TARGET;
        this.uses = 10;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        World world = player.world;

        this.castTarget(player, (result, beam) -> {
            beam.remove();
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = ((BlockRayTraceResult)result).getPos();
                BlockPos pos1 = pos.up(1);
                BlockPos pos2 = pos.up(2);

                if (WorldHelper.isSolidishBlock(world, pos)
                    && WorldHelper.isAirBlock(world, pos1)
                    && WorldHelper.isAirBlock(world, pos2)
                ) {
                    player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
                    player.setMotion(0, 0, 0);
                    didCast.accept(true);
                    return;
                }
            }
            didCast.accept(false);
        });
    }
}
