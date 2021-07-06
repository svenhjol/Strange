package svenhjol.strange.module.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.event.EntityDropItemsCallback;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID, description = "Additional configuration options for mobs that spawn in Strange.")
public class Mobs extends CharmModule {
    @Config(name = "Allow Illusioners", description = "If true, Illusioners will be allowed to spawn at stone circles.  Like Evokers, they drop a Totem of Undying when killed.")
    public static boolean illusioners = true;

    @Override
    public void runWhenEnabled() {
        EntityDropItemsCallback.AFTER.register(this::tryDropTotemFromIllusioner);
    }

    private InteractionResult tryDropTotemFromIllusioner(LivingEntity entity, DamageSource source, int lootingLevel) {
        if (illusioners && !entity.level.isClientSide && entity instanceof Illusioner) {
            Level world = entity.getCommandSenderWorld();
            BlockPos pos = entity.blockPosition();
            world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, new ItemStack(Items.TOTEM_OF_UNDYING)));
        }

        return InteractionResult.PASS;
    }
}
