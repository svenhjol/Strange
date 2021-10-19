package svenhjol.strange.module.runestones;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals.JournalHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RunestoneScreen extends AbstractContainerScreen<RunestoneMenu> {
    private final int UNKNOWN_COLOR = 0xDDCCBB;
    private final int KNOWN_COLOR = 0xFFFFFF;
    private final int WRAP_AT = 14;

    private Random random;
    private IRunestoneMaterial material;
    private ResourceLocation texture;
    private int itemRandomTicks = 0;
    private NonNullList<ItemStack> items;

    public RunestoneScreen(RunestoneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.passEvents = false;
        this.random = new Random(menu.containerId);

        // ask server to update the player journal
        JournalsClient.sendSyncJournal();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBg(poseStack, delta, mouseX, mouseY);

        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);

        getDestination().ifPresent(destination -> {
            ClientHelper.getPlayer().flatMap(Journals::getPlayerData).ifPresent(journal -> {
                renderRunes(poseStack, destination, journal);
                renderLocationClue(poseStack, destination, journal);
                renderItemClue(poseStack, mouseX, mouseY, destination, journal);
                renderTooltip(poseStack, mouseX, mouseY);
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

    protected void renderRunes(PoseStack poseStack, Destination destination, JournalData journal) {
        if (this.material == null) {
            this.material = menu.getMaterial();
        }

        String runeString = destination.runes;
        String knownRuneString = KnowledgeHelper.convertRunesWithLearnedRunes(runeString, journal.getLearnedRunes());

        int mid = width / 2;
        int left = mid - 76;
        int top = (height / 2) - 70;
        int xOffset = 11;
        int yOffset = 14;
        int index = 0;

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 14; x++) {
                if (index < knownRuneString.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(knownRuneString.charAt(index));
                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
                        rune = new TextComponent(KnowledgeHelper.UNKNOWN);
                        color = UNKNOWN_COLOR;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = KNOWN_COLOR;
                    }

                    font.drawShadow(poseStack, rune, left + (x * xOffset), top + (y * yOffset), color);
                }
                index++;
            }
        }
    }

    protected void renderLocationClue(PoseStack poseStack, Destination destination, JournalData journal) {
        String name;
        int numberOfUnknownRunes = JournalHelper.getNumberOfUnknownRunes(destination.runes, journal);

        if (numberOfUnknownRunes > 0 && numberOfUnknownRunes < 5) {
            name = I18n.get("gui.strange.clues." + destination.clue) + "...";
        } else if (numberOfUnknownRunes == 0) {
            name = StringHelper.snakeToPretty(destination.location.getPath());
        } else {
            return;
        }

        int mid = width / 2;
        int left = mid - 76;
        int top = (height / 2) - (destination.runes.length() > WRAP_AT ? 44 : 54);

        font.drawShadow(poseStack, name, left, top, KNOWN_COLOR);
    }

    protected void renderItemClue(PoseStack poseStack, int mouseX, int mouseY, Destination destination, JournalData journal) {
        List<Component> text = Lists.newArrayList();
        Slot slot = menu.getSlot(0);
        if (slot.hasItem()) {
            return;
        }

        int numberOfUnknownRunes = JournalHelper.getNumberOfUnknownRunes(destination.runes, journal);
        if (numberOfUnknownRunes > destination.items.size()) {
            return;
        }

        if (items == null || itemRandomTicks++ >= 100) {
            items = NonNullList.create();
            NonNullList<ItemStack> sublist = NonNullList.create();
            destination.items.forEach(item -> items.add(new ItemStack(item)));

            if (numberOfUnknownRunes > 0) {
                sublist.addAll(items.subList(0, Math.min(items.size(), numberOfUnknownRunes + 1)));
                Collections.shuffle(sublist, new Random());
            } else {
                sublist.addAll(items.subList(0, 1));
            }

            items = sublist;
            itemRandomTicks = 0;
        }

        if (numberOfUnknownRunes > 0) {
            text.add(new TranslatableComponent("gui.strange.clues.possible_items"));
        } else {
            text.add(new TranslatableComponent("gui.strange.clues.required_item"));
        }

        int mid = width / 2;
        int top = (height / 2) - 28;
        int left = mid - 8;
        int bottom = (height / 2) - 12;
        int right = mid + 8;

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

    private Optional<Destination> getDestination() {
        return Optional.ofNullable(RunestonesClient.activeDestination);
    }

    @Override
    public void onClose() {
        RunestonesClient.activeDestination = null;
        super.onClose();
    }
}
