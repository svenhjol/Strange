package svenhjol.strange.module.runestones;

import com.google.common.collect.Lists;
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
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.knowledge.types.Discovery;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.helper.RunestoneHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("ConstantConditions")
public class RunestoneScreen extends AbstractContainerScreen<RunestoneMenu> {
    public static final Component POSSIBLE_ITEMS;
    public static final Component REQUIRED_ITEM;
    public static final Component ACTIVATE;

    private final int UNKNOWN_COLOR = 0xDDCCBB;
    private final int KNOWN_COLOR = 0xFFFFFF;
    private final int WRAP_AT = 14;

    private Random random;
    private IRunestoneMaterial material;
    private ResourceLocation texture;
    private NonNullList<ItemStack> items;
    private int itemRandomTicks = 0;
    private int midX;
    private int midY;

    private Button activateButton;

    public RunestoneScreen(RunestoneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.passEvents = false;
        this.random = new Random(menu.containerId);

        // ask server to update the player journal
        JournalsClient.sendSyncJournal();
    }

    @Override
    protected void init() {
        super.init();

        midX = width / 2;
        midY = height / 2;

        activateButton = addRenderableWidget(new Button(midX - 45, midY + 94, 90, 20, ACTIVATE, b -> {
            syncClickedButton(1);
            onClose();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        activateButton.active = menu.slots.get(0).hasItem();

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);

        if (material == null) {
            material = menu.getMaterial();
        }

        getDestination().ifPresent(destination -> {
            ClientHelper.getPlayer().flatMap(Journals::getJournalData).ifPresent(journal -> {
                renderRunes(poseStack, destination);
                renderLocationClue(poseStack, destination, journal);
                renderItemClue(poseStack, mouseX, mouseY, destination, journal);
            });
        });
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

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

    protected void renderRunes(PoseStack poseStack, Discovery discovery) {
        KnowledgeClient.renderRunesString(
            minecraft,
            poseStack,
            discovery.getRunes(),
            midX - 76,
            midY - 70,
            11,
            14,
            14,
            4,
            KNOWN_COLOR,
            UNKNOWN_COLOR,
            true
        );
    }

    protected void renderLocationClue(PoseStack poseStack, Discovery discovery, JournalData journal) {
        String name;
        int numberOfUnknownRunes = JournalHelper.getNumberOfUnknownRunes(discovery.getRunes(), journal);

        if (numberOfUnknownRunes > 0 && numberOfUnknownRunes < Runestones.SHOW_TEXT_CLUE) {
            String clue = RunestoneHelper.getClue(discovery.getId(), new Random(discovery.getSeed()));
            name = I18n.get("gui.strange.clues." + clue) + "...";
        } else if (numberOfUnknownRunes == 0) {
            name = StringHelper.snakeToPretty(discovery.getId().getPath());
        } else {
            return;
        }

        int mid = width / 2;
        int left = mid - 76;
        int top = (height / 2) - (discovery.getRunes().length() > WRAP_AT ? 44 : 54);

        font.drawShadow(poseStack, name, left, top, KNOWN_COLOR);
    }

    protected void renderItemClue(PoseStack poseStack, int mouseX, int mouseY, Discovery discovery, JournalData journal) {
        List<Component> text = Lists.newArrayList();
        Slot slot = menu.getSlot(0);
        if (slot.hasItem()) {
            return;
        }

        if (minecraft == null || minecraft.level == null) return;
        ResourceLocation dimension = DimensionHelper.getDimension(minecraft.level);
        List<Item> items = RunestoneHelper.getItems(dimension, discovery.getRunes());

        int numberOfUnknownRunes = JournalHelper.getNumberOfUnknownRunes(discovery.getRunes(), journal);
        if (numberOfUnknownRunes > items.size()) {
            return;
        }

        if (this.items == null || itemRandomTicks++ >= 100) {
            this.items = NonNullList.create();
            NonNullList<ItemStack> sublist = NonNullList.create();
            items.forEach(item -> this.items.add(new ItemStack(item)));

            if (numberOfUnknownRunes > 0) {
                sublist.addAll(this.items.subList(0, Math.min(this.items.size(), numberOfUnknownRunes + 1)));
                Collections.shuffle(sublist, new Random());
            } else {
                sublist.addAll(this.items.subList(0, 1));
            }

            this.items = sublist;
            itemRandomTicks = 0;
        }

        if (numberOfUnknownRunes > 0) {
            text.add(POSSIBLE_ITEMS);
            for (ItemStack item : this.items) {
                text.add(item.getHoverName());
            }
        } else {
            text.add(REQUIRED_ITEM);
            text.add(this.items.get(0).getHoverName());
        }

        int top = midY - 28;
        int left = midX - 8;
        int bottom = midY - 12;
        int right = midX + 8;

        if (mouseX > left && mouseX < right && mouseY > top && mouseY < bottom) {
            renderTooltip(poseStack, text, Optional.of(new RunestoneItemTooltip(this.items)), mouseX, mouseY);
        }
    }

    private ResourceLocation getBackgroundTexture() {
        if (texture == null) {
            material = menu.getMaterial();
            texture = new ResourceLocation(Strange.MOD_ID, "textures/gui/" + material.getSerializedName() + "_runestone.png");
        }

        return texture;
    }

    private Optional<Discovery> getDestination() {
        return Optional.ofNullable(RunestonesClient.discoveryHolder);
    }

    private void syncClickedButton(int r) {
        ClientHelper.getClient().ifPresent(mc -> mc.gameMode.handleInventoryButtonClick((this.menu).containerId, r));
    }

    @Override
    public void onClose() {
        RunestonesClient.discoveryHolder = null;
        super.onClose();
    }

    static {
        ACTIVATE = new TranslatableComponent("gui.strange.runestones.activate");
        POSSIBLE_ITEMS = new TranslatableComponent("gui.strange.clues.possible_items");
        REQUIRED_ITEM = new TranslatableComponent("gui.strange.clues.required_item");
    }
}
