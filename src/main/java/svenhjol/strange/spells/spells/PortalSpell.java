package svenhjol.strange.spells.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.spells.module.Spells;

import java.util.function.Consumer;

public class PortalSpell extends Spell
{
    public PortalSpell()
    {
        super("portal");
        this.element = Element.FIRE;
        this.affect = Affect.SELF;
        this.applyCost = 5;
        this.duration = 3.5F;
        this.castCost = 50;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castSelf(player, p -> {
            if (p.dimension == DimensionType.OVERWORLD) {
                PlayerHelper.teleport(p, p.getPosition(), -1, t -> {
                    PlayerQueueHandler.add(t.world.getGameTime() + 10, t, pt -> {
                        for (int i = 0; i < 3; i++) {
                            BlockPos pp = pt.getPosition().add(0, i, 0);
                            BlockState state = pt.world.getBlockState(pp);
                            if (state.isSolid()) {
                                pt.world.setBlockState(pp, Blocks.AIR.getDefaultState(), 2);
                            }
                        }
                        pt.setPositionAndUpdate(pt.getPosition().getX() + 0.5D, pt.getPosition().getY() + 1.0D, pt.getPosition().getZ() + 0.5D);
                        Spells.effectEnchant((ServerWorld)pt.world, pt.getPositionVec(), this, 10, 0, 0, 0, 0.05D);
                        pt.world.playSound(null, pt.getPosition(), StrangeSounds.RUNESTONE_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    });
                });
            } else {
                PlayerHelper.teleportSurface(p, p.getPosition(), 0, t -> {
                    t.setPositionAndUpdate(t.getPosition().getX() + 0.5D, t.getPosition().getY() + 1.0D, t.getPosition().getZ() + 0.5D);
                });
            }
            didCast.accept(true);
        });
    }
}
