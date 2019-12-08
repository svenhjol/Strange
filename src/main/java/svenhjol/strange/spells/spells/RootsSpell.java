package svenhjol.strange.spells.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RootsSpell extends Spell
{
    public RootsSpell()
    {
        super("roots");
        this.element = Element.EARTH;
        this.affect = Affect.FOCUS;
        this.applyCost = 1;
        this.duration = 1.0F;
        this.castCost = 4;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        World world = player.world;

        castFocus(player, result -> {

            int f = 1;
            for (int tx = 0; tx < 6; tx++) {
                for (int tz = 0; tz < 6; tz++) {
                    for (int tt = 0; tt <= 1; tt++) {
                        BlockPos pos = result.getPos().add(tx * f, 0, tz * f);
                        BlockState state = world.getBlockState(pos);
                        if (state.getMaterial() == Material.WOOD
                            && state.getBlock() instanceof LogBlock
                        ) {
                            boolean validTarget = false;
                            for (int i = 0; i < 10; i++) {
                                BlockPos testPos = pos.add(0, i, 0);
                                BlockState testState = world.getBlockState(testPos);
                                if (testState.getMaterial() != Material.WOOD
                                    || testState.getMaterial() != Material.LEAVES) {
                                    if (world.isAirBlock(testPos.up(1))) {
                                        validTarget = true;
                                    }
                                } else {
                                    break;
                                }
                            }

                            if (validTarget) {
                                List<Direction> directions = Arrays.stream(Direction.values())
                                    .filter(d -> d.getHorizontalIndex() >= 0)
                                    .collect(Collectors.toList());
                                Collections.shuffle(directions);

                                for (Direction d : directions) {
                                    if (d.getHorizontalIndex() < 0) continue;
                                    if (world.isAirBlock(pos.offset(d, 1))) {
                                        BlockPos newPos = pos.offset(d, 1);
                                        player.setPositionAndUpdate(newPos.getX() + 0.5D, newPos.getY() + 1D, newPos.getZ() + 0.5D);
                                        didCast.accept(true);
                                        return;
                                    }
                                }
                            }
                        }

                        f = -f;
                    }
                }
            }

            didCast.accept(false);
        });
    }
}
