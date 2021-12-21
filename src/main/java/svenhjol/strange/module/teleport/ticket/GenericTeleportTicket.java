package svenhjol.strange.module.teleport.ticket;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import svenhjol.charm.helper.LogHelper;

public class GenericTeleportTicket extends TeleportTicket {
    public GenericTeleportTicket(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to) {
        super(entity, dimension, from, to);
    }

    @Override
    public void success() {
        super.success();
        LogHelper.debug(getClass(), "Successfully finished entity teleport.");
    }

    @Override
    public void fail() {
        super.fail();
        LogHelper.warn(getClass(), "Failed to complete entity teleport.");
    }
}
