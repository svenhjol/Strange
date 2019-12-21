package svenhjol.strange.spells.spells;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import svenhjol.strange.base.helper.StructureHelper;

import java.util.function.Consumer;

public class BlinkSpell extends Spell
{
    public BlinkSpell()
    {
        super("blink");
        this.element = Element.AIR;
        this.affect = Affect.FOCUS;
        this.applyCost = 2;
        this.duration = 1.25F;
        this.castCost = 8;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        World world = player.world;

        this.castTarget(player, (result, beam) -> {
            beam.remove();
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = ((BlockRayTraceResult)result).getPos();
                BlockPos pos1 = pos.up(1);
                BlockPos pos2 = pos.up(2);

                if (StructureHelper.isSolidishBlock(world, pos)
                    && StructureHelper.isAirBlock(world, pos1)
                    && StructureHelper.isAirBlock(world, pos2)
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
