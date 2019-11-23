package svenhjol.strange.magic.spells;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class GrowthSpell extends Spell
{
    public GrowthSpell()
    {
        super("growth");
        this.element = Element.EARTH;
        this.effect = Effect.AREA;
    }

    @Override
    public boolean cast(PlayerEntity player, ItemStack staff)
    {
        this.castArea(player, 4, blocks -> {
            World world = player.world;

            for (BlockPos pos : blocks) {
                boolean didGrow = false;
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof IPlantable) {
                    if (block instanceof IGrowable) {
                        IGrowable growable = (IGrowable)state.getBlock();
                        if (growable.canGrow(world, pos, state, world.isRemote)) {
                            growable.grow(world, world.rand, pos, state);
                            didGrow = true;
                        }
                    }
                }
                if (block instanceof NetherWartBlock && state.has(NetherWartBlock.AGE)) {
                    Integer currentAge = state.get(NetherWartBlock.AGE);
                    if (currentAge < 3) {
                        world.setBlockState(pos, state.with(NetherWartBlock.AGE, ++currentAge), 2);
                        didGrow = true;
                    }
                }
                if (block instanceof SugarCaneBlock && world.isAirBlock(pos.up()) && state.has(SugarCaneBlock.AGE)) {
                    Integer currentAge = state.get(SugarCaneBlock.AGE);
                    if (currentAge < 15) {
                        world.setBlockState(pos, state.with(SugarCaneBlock.AGE, ++currentAge), 2);
                        SugarCaneBlock sugarCaneBlock = (SugarCaneBlock)block;
                        sugarCaneBlock.tick(state, world, pos, world.rand);
                        didGrow = true;
                    }
                }

                if (didGrow) world.playEvent(2005, pos, 0);
            }
        });

        return true;
    }
}
