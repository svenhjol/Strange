package svenhjol.strange.magic.spells;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.meson.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SummonSpell extends Spell
{
    public SummonSpell()
    {
        super("summon");
        this.element = Element.EARTH;
        this.affect = Affect.AREA;
        this.quantity = 8;
        this.duration = 3.5F;
        this.castCost = 20;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        int[] range = {16, 2, 16};
        this.castArea(player, range, blocks -> {
            World world = player.world;

            List<MonsterEntity> entities;
            AxisAlignedBB area = player.getBoundingBox().grow(range[0], range[1], range[2]);
            Predicate<MonsterEntity> selector = Objects::nonNull;
            entities = world.getEntitiesWithinAABB(MonsterEntity.class, area, selector);

            if (!entities.isEmpty()) {
                List<WolfEntity> spawned = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    WolfEntity fighter = EntityType.WOLF.create(world);
                    spawned.add(fighter);
                    MonsterEntity target = entities.get(world.rand.nextInt(entities.size()));

                    PlayerHelper.spawnEntityNearPlayer(player, fighter, (mobEntity, blockPos) -> {
                        WolfEntity s = (WolfEntity) mobEntity;
                        s.setHealth(8.0F);
                        s.setAttackTarget(target);
                    });
                }

                PlayerQueueHandler.add(world.getGameTime() + 200, player, p -> {
                    for (WolfEntity fighter : spawned) {
                        fighter.remove();
                    }
                });
            }
        });

        didCast.accept(true);
    }
}
