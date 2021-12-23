package svenhjol.strange.module.journals.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.MapHelper;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.bookmarks.Bookmark;

@Id("strange:make_map")
public class ServerReceiveMakeMap extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);
            if (!DimensionHelper.isDimension(player.level, bookmark.getDimension())) return;

            Inventory inventory = player.getInventory();
            int slot = inventory.findSlotMatchingItem(new ItemStack(Items.MAP));
            if (slot == -1) {
                LogHelper.warn(getClass(), "No map found in any inventory slot");
                return;
            }
            LogHelper.debug(getClass(), "Found empty map at slot " + slot);

            BlockPos pos = bookmark.getBlockPos();
            String name = bookmark.getName();
            MapDecoration.Type decoration = MapDecoration.Type.TARGET_X;
            ItemStack map = MapHelper.create((ServerLevel)player.level, pos, new TextComponent(name), decoration, 0x000000);

            inventory.setItem(slot, ItemStack.EMPTY);
            LogHelper.debug(getClass(), "Setting slot " + slot + " to empty");

            ItemStack held = player.getMainHandItem().copy();
            LogHelper.debug(getClass(), "Player was holding: " + held + ", setting to new map");
            player.setItemInHand(InteractionHand.MAIN_HAND, map);

            LogHelper.debug(getClass(), "Attempting to place previously held item " + held + " back to inventory");
            inventory.placeItemBackInInventory(held);
        });
    }
}
