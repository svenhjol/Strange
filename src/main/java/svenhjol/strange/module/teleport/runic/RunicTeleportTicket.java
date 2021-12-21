package svenhjol.strange.module.teleport.runic;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.teleport.ticket.TeleportTicket;

public class RunicTeleportTicket extends TeleportTicket {
    public RunicTeleportTicket(LivingEntity entity, ResourceLocation dimension, BlockPos from, BlockPos to) {
        super(entity, dimension, from, to);
    }

    @Override
    public void fail() {
        LogHelper.warn(getClass(), "Teleport ticket failed, blowing up the origin");
        WorldHelper.explode(getLevel(), getFrom(), 2.0F, Explosion.BlockInteraction.BREAK);
    }
}
