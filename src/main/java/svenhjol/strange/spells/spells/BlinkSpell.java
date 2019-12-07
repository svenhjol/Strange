package svenhjol.strange.spells.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BlinkSpell extends Spell
{
    public BlinkSpell()
    {
        super("blink");
        this.element = Element.AIR;
        this.affect = Affect.TARGET;
        this.applyCost = 2;
        this.duration = 1.0F;
        this.castCost = 6;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        List<RayTraceResult.Type> respondTo = new ArrayList<>(Arrays.asList(RayTraceResult.Type.BLOCK));
        this.castTarget(player, respondTo, (result, beam) -> {
            World world = player.world;
            BlockPos pos = ((BlockRayTraceResult) result).getPos();
            BlockState state = world.getBlockState(pos);
            beam.remove();

            if ((state.isSolid()
                || state.getMaterial() == Material.WATER
                || state.getMaterial() == Material.LEAVES
                || state.getMaterial() == Material.PLANTS
            )
                && !world.getBlockState(pos.up(1)).isSolid()
                && !world.getBlockState(pos.up(2)).isSolid()
            ) {
                player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
                didCast.accept(true);
                return;
            }
            didCast.accept(false);
        });
    }
}
