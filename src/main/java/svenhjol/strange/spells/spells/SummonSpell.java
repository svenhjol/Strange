package svenhjol.strange.spells.spells;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import svenhjol.meson.helper.PlayerHelper;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SummonSpell extends Spell
{
    public static final String SUMMONED = "strange_summoned";

    public SummonSpell()
    {
        super("summon");
        this.color = DyeColor.GRAY;
        this.affect = Affect.FOCUS;
        this.applyCost = 5;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack stone, Consumer<Boolean> didCast)
    {
        int[] range = {6, 2, 6};
        this.castFocus(player, pos -> {
            World world = player.world;

            List<MonsterEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<MonsterEntity> selector = Objects::nonNull;
            entities = world.getEntitiesWithinAABB(MonsterEntity.class, area, selector);

            if (!entities.isEmpty()) {
                IronGolemEntity fighter = EntityType.IRON_GOLEM.create(world);
                if (fighter != null) {
                    fighter.getTags().add(SUMMONED);
                    MonsterEntity target = entities.get(world.rand.nextInt(entities.size()));

                    PlayerHelper.spawnEntityNearPlayer(player, fighter, (mobEntity, blockPos) -> {
                        IronGolemEntity s = (IronGolemEntity) mobEntity;
                        s.setAttackTarget(target);
                    });

                    didCast.accept(true);
                    return;
                }
            }

            didCast.accept(false);
        });
    }
}
