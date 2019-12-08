package svenhjol.strange.spells.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

        castFocus(player, result -> {
            BlockPos pos = result.getPos();
            BlockState state = world.getBlockState(pos);

            if ((state.isSolid()
                || state.getMaterial() == Material.WATER
                || state.getMaterial() == Material.LEAVES
                || state.getMaterial() == Material.PLANTS
                || state.getMaterial() == Material.ORGANIC
                || state.getBlock() == Blocks.GRASS
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
