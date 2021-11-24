package svenhjol.strange.module.ender_bundles;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.bundle_sorting.BundleSorting;
import svenhjol.charm.module.bundle_sorting.event.SortBundleItemsCallback;
import svenhjol.strange.Strange;

import java.util.LinkedList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Ender bundles allow transfer of items to and from your ender chest.")
public class EnderBundles extends CharmModule {
    public static final String TAG_ENDER_ITEMS = "EnderItems";

    public static final ResourceLocation MSG_SERVER_UPDATE_ENDER_INVENTORY = new ResourceLocation(Strange.MOD_ID, "server_update_ender_inventory");
    public static final ResourceLocation MSG_CLIENT_UPDATE_ENDER_INVENTORY = new ResourceLocation(Strange.MOD_ID, "server_client_ender_inventory");
    public static final ResourceLocation TRIGGER_USED_ENDER_BUNDLE = new ResourceLocation(Strange.MOD_ID, "used_ender_bundle");

    public static EnderBundleItem ENDER_BUNDLE;

    @Override
    public void register() {
        ENDER_BUNDLE = new EnderBundleItem(this);
        BundleSorting.SORTABLE.add(ENDER_BUNDLE);
    }

    @Override
    public void runWhenEnabled() {
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_ENDER_INVENTORY, this::handleUpdateEnderInventory);
        SortBundleItemsCallback.EVENT.register(this::handleCycleBundleContents);
    }

    private void handleCycleBundleContents(ServerPlayer player, ItemStack stack, boolean direction) {
        if (stack.getItem() == EnderBundles.ENDER_BUNDLE) {
            PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
            List<ItemStack> contents = new LinkedList<>();

            for (int i = 0; i < enderChestInventory.getContainerSize(); i++) {
                ItemStack s = enderChestInventory.getItem(i);
                if (!s.isEmpty())
                    contents.add(s);
            }

            SortBundleItemsCallback.sortByScrollDirection(contents, direction);
            enderChestInventory.clearContent();
            contents.forEach(enderChestInventory::addItem);
        }
    }

    private void handleUpdateEnderInventory(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> {
            CompoundTag tag = new CompoundTag();
            tag.put(TAG_ENDER_ITEMS, player.getEnderChestInventory().createTag());
            NetworkHelper.sendPacketToClient(player, MSG_CLIENT_UPDATE_ENDER_INVENTORY, buf -> buf.writeNbt(tag));
        });
    }

    public static void triggerUsedEnderBundle(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, EnderBundles.TRIGGER_USED_ENDER_BUNDLE);
    }
}