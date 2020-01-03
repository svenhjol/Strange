package svenhjol.strange.spells.spells;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class LevitateSpell extends Spell
{
    public LevitateSpell()
    {
        super("levitate");
        this.color = DyeColor.PINK;
        this.affect = Affect.TARGET;
        this.uses = 3;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        World world = player.world;

        this.castTarget(player, (result, beam) -> {
            Entity e = getClosestEntity(world, result);
            beam.remove();
            if (e instanceof MobEntity) {
                MobEntity mob = (MobEntity) e;
                mob.addPotionEffect(new EffectInstance(Effects.LEVITATION, 140, 0));
                didCast.accept(true);
                return;
            }
            didCast.accept(false);
        });
    }
}
