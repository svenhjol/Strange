package svenhjol.strange.spells.spells;

import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Consumer;

public class LightningSpell extends Spell
{
    public LightningSpell()
    {
        super("lightning");
        this.color = DyeColor.YELLOW;
        this.affect = Affect.TARGET;
        this.applyCost = 2;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            BlockPos pos = null;

            if (result.getType() == RayTraceResult.Type.BLOCK) {
                pos = ((BlockRayTraceResult)result).getPos();
            } else if (result.getType() == RayTraceResult.Type.ENTITY) {
                pos = ((EntityRayTraceResult)result).getEntity().getPosition();
            }

            if (pos != null) {
                beam.remove();
                LightningBoltEntity lightning = new LightningBoltEntity(player.world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, false);
                lightning.setCaster(player instanceof ServerPlayerEntity ? (ServerPlayerEntity)player : null);
                ((ServerWorld)player.world).addLightningBolt(lightning);
                didCast.accept(true);
                return;
            }

            didCast.accept(false);
        });
    }
}
