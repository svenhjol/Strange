package svenhjol.strange.module.teleport.ticket;

import net.minecraft.world.entity.LivingEntity;
import svenhjol.charm.helper.LogHelper;

public class GenericRepositionTicket extends RepositionTicket {
    public GenericRepositionTicket(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void success() {
        super.success();
        LogHelper.debug(getClass(), "Successfully finished entity reposition.");
    }

    @Override
    public void fail() {
        super.fail();
        LogHelper.warn(getClass(), "Failed to complete entity reposition.");
    }
}
