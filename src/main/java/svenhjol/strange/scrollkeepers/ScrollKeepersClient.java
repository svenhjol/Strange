package svenhjol.strange.scrollkeepers;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.strange.scrolls.ScrollItem;
import svenhjol.strange.scrolls.ScrollsHelper;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.List;

public class ScrollKeepersClient extends CharmClientModule {
    public ScrollKeepersClient(CharmModule module) {
        super(module);
    }

    private static Quest heldScrollQuest;

    @Override
    public void register() {
        PlayerTickCallback.EVENT.register(this::villagerInterested);

        // listen for quest tag being sent from the server to this player
        ClientPlayNetworking.registerGlobalReceiver(Scrollkeepers.MSG_CLIENT_RECEIVE_SCROLL_QUEST, this::handleReceiveScrollQuest);

        // cut-out for 3D writing desk
        BlockRenderLayerMap.INSTANCE.putBlock(Scrollkeepers.WRITING_DESK, RenderLayer.getCutout());
    }

    public void villagerInterested(PlayerEntity player) {
        if (player.world.isClient && player.world.getTime() % 20 == 0) {
            World world = player.world;
            BlockPos playerPos = player.getBlockPos();
            int range = Scrollkeepers.interestRange;

            // get held item
            ItemStack held = player.getMainHandStack();
            if (!(held.getItem() instanceof ScrollItem))
                return;

            String questId = ScrollItem.getScrollQuest(held);
            if (questId == null)
                return;

            if (!player.getUuid().equals(ScrollItem.getScrollOwner(held)))
                return;

            // fire a request to the server to fetch the quest data for this questId
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeString(questId);
            ClientPlayNetworking.send(Scrollkeepers.MSG_SERVER_GET_SCROLL_QUEST, buffer);

            if (heldScrollQuest == null || !heldScrollQuest.getId().equals(questId))
                return;

            int x = playerPos.getX();
            int y = playerPos.getY();
            int z = playerPos.getZ();

            // get villagers in range
            List<VillagerEntity> villagers = world.getNonSpectatingEntities(VillagerEntity.class, new Box(
                x - range, y - range, z - range, x + range, y + range, z + range
            ));

            if (villagers.isEmpty())
                return;

            if (!heldScrollQuest.isSatisfied(player))
                return;

            villagers.forEach(villager -> {
                if (villager.getVillagerData().getProfession() == Scrollkeepers.SCROLLKEEPER) {
                    if (heldScrollQuest.getMerchant().equals(ScrollsHelper.ANY_UUID) || heldScrollQuest.getMerchant().equals(villager.getUuid()))
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

    private void handleReceiveScrollQuest(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound compoundTag = data.readCompound();
        if (compoundTag == null || compoundTag.isEmpty())
            return;

        Quest quest = Quest.getFromTag(compoundTag);
        client.execute(() -> heldScrollQuest = quest);
    }
}
