package svenhjol.strange.feature.ender_bundles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.CharmClient;
import svenhjol.charm.feature.portable_crafting.PortableCrafting;
import svenhjol.charm.mixin.accessor.AbstractContainerScreenAccessor;
import svenhjol.charm_api.event.*;
import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.helper.ScreenHelper;
import svenhjol.charm_core.init.CoreResources;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeClient;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@ClientFeature
public class EnderBundlesClient extends CharmFeature {
    private static final int CHECK_TICKS_SLOW = 60;
    private static final int CHECK_TICKS_FAST = 5;
    private static final int LEFT = 76;
    public static float CACHED_AMOUNT_FILLED = 0F;
    public static Supplier<String> OPEN_BUNDLE_KEY;

    public ImageButton enderButton;

    @Override
    public List<BooleanSupplier> checks() {
        return List.of(() -> Strange.LOADER.isEnabled(EnderBundles.class));
    }

    @Override
    public void register() {
        OPEN_BUNDLE_KEY = CharmClient.REGISTRY.key("open_bundle",
            () -> new KeyMapping("key.charm.open_ender_bundle", GLFW.GLFW_KEY_B, "key.categories.inventory"));

        if (isEnabled()) {
            StrangeClient.REGISTRY.itemTab(
                EnderBundles.ITEM,
                CreativeModeTabs.TOOLS_AND_UTILITIES,
                Items.LEAD
            );
        }
    }

    @Override
    public void runWhenEnabled() {
        ClientTickEvent.INSTANCE.handle(this::handleClientTick);
        KeyPressEvent.INSTANCE.handle(this::handleKeyPress);

        ScreenSetupEvent.INSTANCE.handle(this::handleSetupScreen);
        ScreenRenderEvent.INSTANCE.handle(this::handleRenderScreen);

        TooltipComponentEvent.INSTANCE.handle(this::handleTooltipComponent);

        ItemProperties.register(EnderBundles.ITEM.get(), new ResourceLocation("ender_bundle_filled"),
            (stack, level, entity, i) -> getAmountFilled());
    }

    private Optional<TooltipComponent> handleTooltipComponent(ItemStack stack) {
        if (stack != null && stack.is(EnderBundles.ITEM.get())) {
            var client = Minecraft.getInstance();
            if (client.player == null) return Optional.empty();

            // Make much more frequent client inventory requests when hovering.
            if (client.level != null && client.level.getGameTime() % 5 == 0) {
                EnderBundlesClient.requestEnderInventory();
            }

            var items = EnderBundles.getEnderInventory(client.player);
            return Optional.of(new EnderBundleItemTooltip(items));
        }

        return Optional.empty();
    }

    private void handleSetupScreen(Screen screen) {
        var client = Minecraft.getInstance();

        if (client.player == null) return;
        if (!(screen instanceof InventoryScreen inventoryScreen)) return;

        int leftPos = ((AbstractContainerScreenAccessor)inventoryScreen).getLeftPos();
        int midY = inventoryScreen.height / 2;

        midY += PortableCrafting.hasCraftingTable(client.player) ? 18 : 0;

        this.enderButton = new ImageButton(leftPos + LEFT, midY - 66, 20, 18, 20, 0, 19, CoreResources.INVENTORY_BUTTONS,
            click -> openEnderBundle());

        this.enderButton.visible = EnderBundles.hasEnderBundle(client.player);
        ScreenHelper.addRenderableWidget(inventoryScreen, this.enderButton);
    }

    /**
     * Check if it's the inventory screen and show the Ender button if the player has an Ender bundle.
     * @param screen Container from event.
     * @param poseStack PoseStack from event.
     * @param mouseX Mouse X position.
     * @param mouseY Mouse Y position.
     */
    private void handleRenderScreen(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        var client = Minecraft.getInstance();

        if (!(client.screen instanceof InventoryScreen)) return;
        if (this.enderButton == null || client.player == null) return;

        if (client.player.level.getGameTime() % CHECK_TICKS_FAST == 0) {
            this.enderButton.visible = EnderBundles.hasEnderBundle(client.player);
        }

        // Re-render when recipe is opened/closed.
        var x = ((AbstractContainerScreenAccessor)screen).getLeftPos();
        enderButton.setPosition(x + LEFT, enderButton.getY());
    }

    private void handleKeyPress(String id) {
        if (id.equals(OPEN_BUNDLE_KEY.get())) {
            openEnderBundle();
        }
    }

    private void handleClientTick(Minecraft client) {
        if (client == null || client.player == null) return;

        // Do this Ender Inventory check sparingly.
        if (client.player.level.getGameTime() % CHECK_TICKS_SLOW == 0) {
            requestEnderInventory();
        }
    }

    private float getAmountFilled() {
        return CACHED_AMOUNT_FILLED;
    }

    private void openEnderBundle() {
        EnderBundlesNetwork.OpenInventory.send();
    }

    public static void requestEnderInventory() {
        EnderBundlesNetwork.RequestInventory.send();
    }

    public static void handleReceivedInventory(EnderBundlesNetwork.SendInventory request, Player player) {
        var items = request.getItems();

        if (items != null && player != null) {
            var inventory = player.getEnderChestInventory();
            inventory.fromTag(items);
            EnderBundlesClient.CACHED_AMOUNT_FILLED = (float)items.size() / inventory.getContainerSize();
        }
    }
}
