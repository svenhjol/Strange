package svenhjol.strange.spells.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import svenhjol.meson.handler.PlayerQueueHandler;

import java.util.function.Consumer;

public class PacifySpell extends Spell
{
    public PacifySpell()
    {
        super("pacify");
        this.element = Element.WATER;
        this.affect = Affect.TARGET;
        this.duration = 1.0F;
        this.castCost = 5;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        World world = player.world;

        this.castTarget(player, (result, beam) -> {
            Entity e = getClosestEntity(world, result);
            beam.remove();
            if (e instanceof MobEntity) {
                MobEntity mob = (MobEntity) e;
                if (!mob.isAIDisabled()) {
                    mob.setNoAI(true);
                    mob.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 140, 0));
                    PlayerQueueHandler.add(world.getGameTime() + 150, player, p -> mob.setNoAI(false));
                }

                didCast.accept(true);
                return;
            }
            didCast.accept(false);
        });
    }
}
