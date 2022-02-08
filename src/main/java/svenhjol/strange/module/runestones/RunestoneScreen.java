package svenhjol.strange.module.runestones;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.discoveries.DiscoveriesClient;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.discoveries.DiscoveryHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.runes.client.RuneStringRenderer;
import svenhjol.strange.module.runestones.helper.RunestoneHelper;

import java.util.*;

@SuppressWarnings("ConstantConditions")
public class RunestoneScreen extends AbstractContainerScreen<RunestoneMenu> {
    public static final Component POSSIBLE_ITEMS;
    public static final Component REQUIRED_ITEM;
    public static final Component ACTIVATE;

    private final int wrapAt = 14;
    private final int knownColor = 0xffffff;
    private final int unknownColor = 0xddccbb;
    private final Random random;

    private RunestoneMaterial material;
    private ResourceLocation texture;
    private NonNullList<ItemStack> items;
    private List<Item> potentialItems;
    private int itemRandomTicks = 0;
    private int midX;
    private int midY;

    private Button activateButton;
    private RuneStringRenderer runeStringRenderer;

    public RunestoneScreen(RunestoneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.passEvents = false;
        this.random = new Random(menu.containerId);
    }

    @Override
    protected void init() {
        super.init();

        midX = width / 2;
        midY = height / 2;

        var button = new Button(midX - 45, midY + 94, 90, 20, ACTIVATE, b -> {
            syncClickedButton(1);
            onClose();
        });

        // We need a reference to this Activate button so we can enable or
        // disable it according to whether the menu slot has an item in it.
        activateButton = addRenderableWidget(button);
        activateButton.active = false;

        if (material == null) {
            material = menu.getMaterial();
        }

        runeStringRenderer = new RuneStringRenderer(midX - 76, midY - 70, 11, 14, 14, 4);
        runeStringRenderer.setWithShadow(true);
        runeStringRenderer.setKnownColor(knownColor);
        runeStringRenderer.setUnknownColor(unknownColor);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);

        // Need to have a synchronised discovery for the runestone block that the player is looking at.
        var opt = DiscoveriesClient.getInteractedDiscovery();
        if (opt.isEmpty()) return;

        var discovery = opt.get();

        activateButton.active = menu.slots.get(0).hasItem();
        runeStringRenderer.render(poseStack, font, discovery.getRunes());

        renderLocationClue(poseStack, discovery);
        renderItemClue(poseStack, mouseX, mouseY, discovery);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getBackgroundTexture());
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // nah
    }

    protected void renderLocationClue(PoseStack poseStack, Discovery discovery) {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return;

        String name;
        int left = midX - 76;
        int top = midY - (discovery.getRunes().length() > wrapAt ? 44 : 54);
        var unknown = JournalHelper.countUnknownRunes(discovery.getRunes(), journal);

        if (unknown > 0 && unknown < Runestones.SHOW_TEXT_CLUE) {

            // When the number of unknown runes is within a tolerance (SHOW_TEXT_CLUE) then
            // show the player a clue about the location that the runestone links to.
            String clue = RunestoneHelper.getClue(discovery.getLocation(), new Random(discovery.getSeed()));
            name = I18n.get("gui.strange.clues." + clue) + "...";

        } else if (unknown == 0) {

            // The player knows all the runes, there are no unknown. Show the full name of the location.
            name = DiscoveryHelper.getDiscoveryName(discovery);

        } else {

            // Don't show anything, the player hasn't learned enough runes yet.
            return;

        }

        font.drawShadow(poseStack, name, left, top, knownColor);
    }

    protected void renderItemClue(PoseStack poseStack, int mouseX, int mouseY, Discovery discovery) {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return;

        int top = midY - 28;
        int left = midX - 8;
        int bottom = midY - 12;
        int right = midX + 8;
        List<Component> text = new ArrayList<>();
        Slot slot = menu.getSlot(0);

        // Don't render a hover tooltip if the slot is occupied.
        if (slot.hasItem()) return;

        ResourceLocation dimension = DimensionHelper.getDimension(minecraft.level);

        if (potentialItems == null) {
            potentialItems = RunestoneHelper.getItems(dimension, discovery.getRunes());
        }

        // If the player hasn't learned enough runes then exit early.
        int unknown = JournalHelper.countUnknownRunes(discovery.getRunes(), journal);
        if (unknown > 0) return;

        if (items == null || itemRandomTicks++ >= 100) {

            // After 5 seconds delay, recreate the potential items list to be rendered in the tooltip.
            items = NonNullList.create();
            potentialItems.forEach(item -> items.add(new ItemStack(item)));

            Collections.shuffle(items, new Random());

            potentialItems = null;
            itemRandomTicks = 0;
        }

        // Create the tooltip showing multiple potential items.
        text.add(POSSIBLE_ITEMS);
        for (ItemStack item : items) {
            text.add(item.getHoverName());
        }

        // Render out the tooltip when the player is hovering over the slot.
        if (mouseX > left && mouseX < right && mouseY > top && mouseY < bottom) {
            renderTooltip(poseStack, text, Optional.of(new RunestoneItemTooltip(items)), mouseX, mouseY);
        }
    }

    private ResourceLocation getBackgroundTexture() {
        if (texture == null) {
            material = menu.getMaterial();
            texture = new ResourceLocation(Strange.MOD_ID, "textures/gui/" + material.getSerializedName() + "_runestone.png");
        }

        return texture;
    }

    private void syncClickedButton(int r) {
        ClientHelper.getClient().ifPresent(mc -> mc.gameMode.handleInventoryButtonClick((this.menu).containerId, r));
    }

    @Override
    public void onClose() {
        DiscoveriesClient.setInteractedDiscovery(null);
        super.onClose();
    }

    static {
        ACTIVATE = new TranslatableComponent("gui.strange.runestones.activate");
        POSSIBLE_ITEMS = new TranslatableComponent("gui.strange.clues.possible_items");
        REQUIRED_ITEM = new TranslatableComponent("gui.strange.clues.required_item");
    }
}
