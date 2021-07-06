package svenhjol.strange.module.scrollkeepers;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.scrolls.ScrollItem;
import svenhjol.strange.module.scrolls.ScrollHelper;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.List;

@ClientModule(module = Scrollkeepers.class)
public class ScrollKeepersClient extends CharmModule {
    private static Quest heldScrollQuest;

    @Override
    public void register() {
        // listen for quest tag being sent from the server to this player
        ClientPlayNetworking.registerGlobalReceiver(Scrollkeepers.MSG_CLIENT_RECEIVE_SCROLL_QUEST, this::handleReceiveScrollQuest);

        // cut-out for 3D writing desk
        BlockRenderLayerMap.INSTANCE.putBlock(Scrollkeepers.WRITING_DESK, RenderType.cutout());
    }

    @Override
    public void runWhenEnabled() {
        PlayerTickCallback.EVENT.register(this::villagerInterested);
    }

    public void villagerInterested(Player player) {
        if (player.level.isClientSide && player.level.getGameTime() % 20 == 0) {
            Level world = player.level;
            BlockPos playerPos = player.blockPosition();
            int range = Scrollkeepers.interestRange;

            // get held item
            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof ScrollItem))
                return;

            String questId = ScrollItem.getScrollQuest(held);
            if (questId == null)
                return;

            if (!player.getUUID().equals(ScrollItem.getScrollOwner(held)))
                return;

            // fire a request to the server to fetch the quest data for this questId
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeUtf(questId);
            ClientPlayNetworking.send(Scrollkeepers.MSG_SERVER_GET_SCROLL_QUEST, buffer);

            if (heldScrollQuest == null || !heldScrollQuest.getId().equals(questId))
                return;

            int x = playerPos.getX();
            int y = playerPos.getY();
            int z = playerPos.getZ();

            // get villagers in range
            List<Villager> villagers = world.getEntitiesOfClass(Villager.class, new AABB(
                x - range, y - range, z - range, x + range, y + range, z + range
            ));

            if (villagers.isEmpty())
                return;

            if (!heldScrollQuest.isSatisfied(player))
                return;

            villagers.forEach(villager -> {
                if (villager.getVillagerData().getProfession() == Scrollkeepers.SCROLLKEEPER) {
                    if (heldScrollQuest.getMerchant().equals(ScrollHelper.ANY_UUID) || heldScrollQuest.getMerchant().equals(villager.getUUID()))
                        effectShowInterest(villager);
                }
            });
        }
    }

    private void effectShowInterest(Villager villager) {
        double spread = 0.75D;
        for (int i = 0; i < 3; i++) {
            double px = villager.blockPosition().getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = villager.blockPosition().getY() + 2.25D + (Math.random() - 0.5D) * spread;
            double pz = villager.blockPosition().getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            villager.level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0, 0.12D);
        }
    }

    private void handleReceiveScrollQuest(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag compoundTag = data.readNbt();
        if (compoundTag == null || compoundTag.isEmpty())
            return;

        Quest quest = Quest.getFromNbt(compoundTag);
        client.execute(() -> heldScrollQuest = quest);
    }
}
