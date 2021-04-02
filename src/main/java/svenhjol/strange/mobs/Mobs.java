package svenhjol.strange.mobs;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDropsCallback;
import svenhjol.strange.Strange;

import static net.minecraft.item.Items.TOTEM_OF_UNDYING;

@Module(mod = Strange.MOD_ID, description = "Additional configuration options for mobs that spawn in Strange.")
public class Mobs extends CharmModule {
    @Config(name = "Allow Illusioners", description = "If true, Illusioners will be allowed to spawn at stone circles.  Like Evokers, they drop a Totem of Undying when killed.")
    public static boolean illusioners = true;

    @Override
    public void init() {
        EntityDropsCallback.AFTER.register(this::tryDropTotemFromIllusioner);
    }

    public ActionResult tryDropTotemFromIllusioner(LivingEntity entity, DamageSource source, int lootingLevel) {
        if (illusioners
            && !entity.world.isClient
            && entity instanceof IllusionerEntity
        ) {
            World world = entity.getEntityWorld();
            BlockPos pos = entity.getBlockPos();

            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(TOTEM_OF_UNDYING)));
        }

        return ActionResult.PASS;
    }
}
