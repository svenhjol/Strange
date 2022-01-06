package svenhjol.strange.module.teleport.runic.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import svenhjol.strange.module.runes.RuneBranch;

public class DimensionTeleportHandler extends BaseTeleportHandler<ResourceLocation> {
    public DimensionTeleportHandler(RuneBranch<?, ResourceLocation> branch) {
        super(branch);
    }

    @Override
    public boolean process() {
        BlockPos targetPos;
        var targetDimension = value;
        var p = entity.blockPosition();

        // Handle Nether size compression/expansion.
        var nether = Level.NETHER.location();
        if (!originDimension.equals(nether) && targetDimension.equals(nether)) {

            targetPos = new BlockPos(p.getX() / 8, p.getY(), p.getZ() / 8);

        } else if (originDimension.equals(nether) && !targetDimension.equals(nether)) {

            targetPos = new BlockPos(p.getX() * 8, p.getY(), p.getZ() * 8);

        } else {

            targetPos = p;
        }

        return teleport(targetDimension, targetPos, false, true);
    }
}
