package svenhjol.strange.scrollkeepers;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.strange.scrolls.ScrollHelper;
import svenhjol.strange.scrolls.ScrollItem;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.List;
import java.util.Optional;

public class ScrollKeepersClient extends CharmClientModule {

    public ScrollKeepersClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        PlayerTickCallback.EVENT.register(this::villagerInterested);
    }

    public void villagerInterested(PlayerEntity player) {
        if (player.world.isClient && player.world.getTime() % 10 == 0) {
            World world = player.world;
            BlockPos playerPos = player.getBlockPos();
            int range = Scrollkeepers.interestRange;

            // get held item
            ItemStack heldStack = player.getMainHandStack();
            if (!(heldStack.getItem() instanceof ScrollItem))
                return;

            Optional<Quest> optionalQuest = ScrollItem.getScrollQuest(heldStack);
            if (!optionalQuest.isPresent())
                return;

            Quest quest = optionalQuest.get();

            int x = playerPos.getX();
            int y = playerPos.getY();
            int z = playerPos.getZ();

            // get villagers in range
            List<VillagerEntity> villagers = world.getNonSpectatingEntities(VillagerEntity.class, new Box(
                x - range, y - range, z - range, x + range, y + range, z + range
            ));

            if (villagers.isEmpty())
                return;

            if (!quest.isSatisfied(player))
                return;

            villagers.forEach(villager -> {
                if (villager.getVillagerData().getProfession() == Scrollkeepers.SCROLLKEEPER) {
                    if (quest.getMerchant().equals(ScrollHelper.ANY_UUID) || quest.getMerchant().equals(villager.getUuid()))
                        effectShowInterest(villager);
                }
            });
        }
    }

    private void effectShowInterest(VillagerEntity villager) {
        double spread = 0.75D;
        for (int i = 0; i < 3; i++) {
            double px = villager.getBlockPos().getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = villager.getBlockPos().getY() + 2.25D + (Math.random() - 0.5D) * spread;
            double pz = villager.getBlockPos().getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            villager.world.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0, 0.12D);
        }
    }
}
