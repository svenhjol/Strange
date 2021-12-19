package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.init.StrangeFonts;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals2.helper.Journal2Helper;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.KnowledgeData;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.runes.RuneHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MiniJournalScreen {
    private final List<ButtonDefinition> homeButtons = new ArrayList<>();
    private final Screen screen;

    private JournalSection section;
    private JournalBookmark selectedBookmark;
    private ResourceLocation selectedBiome;
    private ResourceLocation selectedStructure;
    private ResourceLocation selectedDimension;
    private boolean hasRenderedButtons;
    private int lastPage;
    private int midX;
    private int midY;
    private int journalMidX;

    private final int perPage;
    private final int textColor;
    private final int secondaryColor;
    private final int buttonWidth;
    private final int buttonHeight;

    private Font font;
    private ItemRenderer itemRenderer;

    public static final ResourceLocation CLOSED = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal.png");
    public static final ResourceLocation OPEN = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal_open.png");
    public static final Component INCORRECT_DIMENSION;

    public MiniJournalScreen(Screen screen) {
        this.screen = screen;
        this.hasRenderedButtons = false;
        this.section = JournalSection.HOME;
        this.lastPage = 0;
        this.perPage = 6;
        this.textColor = 0x222222;
        this.secondaryColor = 0x908080;
        this.buttonWidth = 88;
        this.buttonHeight = 20;

        this.homeButtons.addAll(Arrays.asList(
            new ButtonDefinition(b -> changeJournalSection(JournalSection.BOOKMARKS), JournalScreen.BOOKMARKS),
            new ButtonDefinition(b -> changeJournalSection(JournalSection.BIOMES), JournalScreen.LEARNED_BIOMES),
            new ButtonDefinition(b -> changeJournalSection(JournalSection.STRUCTURES), JournalScreen.LEARNED_STRUCTURES),
            new ButtonDefinition(b -> changeJournalSection(JournalSection.DIMENSIONS), JournalScreen.LEARNED_DIMENSIONS)
        ));
    }

    public void init() {
        midX = screen.width / 2;
        midY = screen.height / 2;

        if (screen.height % 2 == 0) {
            midY -= 1;
        }

        journalMidX = midX - 88;
        hasRenderedButtons = false;
    }

    public void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        ClientHelper.getClient().ifPresent(mc -> {
            ResourceLocation texture = section == JournalSection.HOME ? CLOSED : OPEN;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, texture);
            screen.blit(poseStack, (screen.width / 2) - 284, (screen.height / 2) - 144, 123, 222, 256, 256);
        });
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, JournalData journal) {
        if (ClientHelper.getClient().isEmpty()) return;
        if (ClientHelper.getPlayer().isEmpty()) return;
        if (KnowledgeClient.getKnowledgeData().isEmpty()) return;

        Minecraft minecraft = ClientHelper.getClient().get();
        Player player = ClientHelper.getPlayer().get();
        KnowledgeData knowledge = KnowledgeClient.getKnowledgeData().get();

        // it's important to set these at this point in the render code otherwise things don't appear properly
        font = screen.font;
        itemRenderer = screen.itemRenderer;

        switch (section) {
            case HOME -> renderHome(poseStack, journal, knowledge, minecraft, player, mouseX, mouseY);
            case BOOKMARKS -> renderBookmarks(poseStack, journal, knowledge, minecraft, player, mouseX, mouseY);
            case BIOMES -> renderBiomes(poseStack, journal, knowledge, minecraft, player, mouseX, mouseY);
            case STRUCTURES -> renderStructures(poseStack, journal, knowledge, minecraft, player, mouseX, mouseY);
            case DIMENSIONS -> renderDimensions(poseStack, journal, knowledge, minecraft, player, mouseX, mouseY);
        }

        hasRenderedButtons = true;
    }

    private void changeJournalSection(JournalSection section) {
        this.section = section;
        redraw();
    }

    private void renderBackButton(Button.OnPress onPress) {
        if (hasRenderedButtons) return;
        int buttonWidth = 74;
        int buttonHeight = 20;
        screen.addRenderableWidget(new Button(journalMidX - 38, midY + 76, buttonWidth, buttonHeight, JournalScreen.GO_BACK, onPress));
    }

    private <T> void paginator(PoseStack poseStack, Font font, List<T> items, Consumer<T> renderItem, Supplier<Component> labelForNoItems, boolean shouldRenderButtons) {
        int paginationY = midY + 50;
        int currentPage = lastPage - 1;
        List<T> sublist;

        int size = items.size();
        if (size > perPage) {
            if (currentPage * perPage >= size || currentPage * perPage < 0) {
                // out of range, reset
                lastPage = 1;
                currentPage = 0;
            }
            sublist = items.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, size));
        } else {
            sublist = items;
        }

        for (T item : sublist) {
            renderItem.accept(item);
        }

        if (size > perPage) {
            TranslatableComponent component = new TranslatableComponent("gui.strange.journal.page", lastPage);
            GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, paginationY + 6, secondaryColor);

            // only render pagination buttons on the first render pass
            if (shouldRenderButtons) {
                if (lastPage * perPage < size) {
                    screen.addRenderableWidget(new ImageButton(midX - 64, paginationY, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
                        ++lastPage;
                        redraw();
                    }));
                }
                if (lastPage > 1) {
                    screen.addRenderableWidget(new ImageButton(midX - 134, paginationY, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
                        --lastPage;
                        redraw();
                    }));
                }
            }
        }

        if (size == 0) {
            GuiHelper.drawCenteredString(poseStack, font, labelForNoItems.get(), journalMidX, midY - 8, secondaryColor);
        }
    }

    private void renderHome(PoseStack poseStack, JournalData journal, KnowledgeData knowledge, Minecraft minecraft, Player player, int mouseX, int mouseY) {
        renderTitle(poseStack, JournalScreen.JOURNAL, midY - 86);

        if (!hasRenderedButtons) {
            int x = journalMidX - (buttonWidth / 2);
            int y = midY - 60; // start rendering buttons from here
            int xOffset = 0;
            int yOffset = 24;

            GuiHelper.addButtons(screen, screen.width, screen.font, homeButtons, buttonWidth, buttonHeight, x, y, xOffset, yOffset);
        }
    }

    private void renderBookmarks(PoseStack poseStack, JournalData journal, KnowledgeData knowledge, Minecraft minecraft, Player player, int mouseX, int mouseY) {
        renderTitle(poseStack, JournalScreen.BOOKMARKS, midY - 94);

        if (selectedBookmark != null) {
            if (!DimensionHelper.isDimension(player.level, selectedBookmark.getDimension())) return;

            renderBackButton(b -> {
                selectedBookmark = null;
                changeJournalSection(JournalSection.BOOKMARKS);
            });

            int y = midY - 78;
            Component component = new TextComponent(getTruncatedName(selectedBookmark.getName(), 18));
            itemRenderer.renderGuiItem(selectedBookmark.getIcon(), journalMidX - 8, y);
            GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, y + 20, textColor);
            renderRunesString(poseStack, selectedBookmark.getRunes(), journalMidX - 46, midY - 8, 9, 14, 10, 4, false);
        } else {
            renderBackButton(b -> changeJournalSection(JournalSection.HOME));

            AtomicInteger y = new AtomicInteger(midY - 78);
            Consumer<JournalBookmark> renderItem = bookmark -> {
                TextComponent bookmarkName = new TextComponent(bookmark.getName());
                String name = getTruncatedName(bookmark.getName(), 14);
                itemRenderer.renderGuiItem(bookmark.getIcon(), journalMidX - (buttonWidth / 2) - 12, y.get() + 2);

                if (!hasRenderedButtons) {
                    boolean correctDimension = DimensionHelper.isDimension(player.level, bookmark.getDimension());

                    // generate the button
                    Button.OnTooltip tooltip = (b, p, i, j) -> screen.renderTooltip(p, correctDimension ? bookmarkName : INCORRECT_DIMENSION, i, j);
                    Button.OnPress press = b -> {
                        selectedBookmark = bookmark;
                        redraw();
                    };
                    Button button = new Button(midX - (buttonWidth / 2) - 82, y.get(), buttonWidth, buttonHeight, new TextComponent(name), press, tooltip);

                    if (!correctDimension) {
                        button.active = false;
                    }
                    screen.addRenderableWidget(button);
                }
                y.addAndGet(21);
            };
            paginator(poseStack, font, journal.getBookmarks(), renderItem, () -> JournalScreen.NO_BOOKMARKS, !hasRenderedButtons);
        }
    }

    private void renderBiomes(PoseStack poseStack, JournalData journal, KnowledgeData knowledge, Minecraft minecraft, Player player, int mouseX, int mouseY) {
        renderTitle(poseStack, JournalScreen.LEARNED_BIOMES, midY - 94);

        if (selectedBiome != null) {
            renderBackButton(b -> {
                selectedBiome = null;
                changeJournalSection(JournalSection.BIOMES);
            });

            knowledge.biomes.get(selectedBiome).ifPresent(runes -> renderResourceLocation(poseStack, selectedBiome, runes, minecraft, mouseX, mouseY));
        } else {
            renderBackButton(b -> changeJournalSection(JournalSection.HOME));
            renderResourceLocations(poseStack, journal.getLearnedBiomes(), () -> JournalScreen.NO_BIOMES, res -> {
                selectedBiome = res;
                redraw();
            });
        }
    }

    private void renderStructures(PoseStack poseStack, JournalData journal, KnowledgeData knowledge, Minecraft minecraft, Player player, int mouseX, int mouseY) {
        renderTitle(poseStack, JournalScreen.LEARNED_STRUCTURES, midY - 94);

        if (selectedStructure != null) {
            renderBackButton(b -> {
                selectedStructure = null;
                changeJournalSection(JournalSection.STRUCTURES);
            });

            knowledge.structures.get(selectedStructure).ifPresent(runes -> renderResourceLocation(poseStack, selectedStructure, runes, minecraft, mouseX, mouseY));
        } else {
            renderBackButton(b -> changeJournalSection(JournalSection.HOME));
            renderResourceLocations(poseStack, journal.getLearnedStructures(), () -> JournalScreen.NO_STRUCTURES, res -> {
                selectedStructure = res;
                redraw();
            });
        }
    }

    private void renderDimensions(PoseStack poseStack, JournalData journal, KnowledgeData knowledge, Minecraft minecraft, Player player, int mouseX, int mouseY) {
        renderTitle(poseStack, JournalScreen.LEARNED_DIMENSIONS, midY - 94);

        if (selectedDimension != null) {
            renderBackButton(b -> {
                selectedDimension = null;
                changeJournalSection(JournalSection.DIMENSIONS);
            });

            knowledge.dimensions.get(selectedDimension).ifPresent(runes -> renderResourceLocation(poseStack, selectedDimension, runes, minecraft, mouseX, mouseY));
        } else {
            renderBackButton(b -> changeJournalSection(JournalSection.HOME));
            renderResourceLocations(poseStack, journal.getLearnedDimensions(), () -> JournalScreen.NO_DIMENSIONS, res -> {
                selectedDimension = res;
                redraw();
            });
        }
    }

    private void renderResourceLocation(PoseStack poseStack, ResourceLocation resource, String runes, Minecraft minecraft, int mouseX, int mouseY) {
        int y = midY - 78;
        Component component = new TextComponent(getTruncatedName(StringHelper.snakeToPretty(resource.getPath(), true), 18));
        GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, y + 20, textColor);
        renderRunesString(poseStack, runes, journalMidX - 46, midY - 8, 9, 14, 10, 4, false);
    }

    private void renderResourceLocations(PoseStack poseStack, List<ResourceLocation> resources, Supplier<Component> labelForNoItem, Consumer<ResourceLocation> onPress) {
        AtomicInteger y = new AtomicInteger(midY - 78);
        Consumer<ResourceLocation> renderItem = res -> {
            String name = getTruncatedName(StringHelper.snakeToPretty(res.getPath(), true), 16);
            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2) - 96, y.get(), 100, buttonHeight, new TextComponent(name), b -> onPress.accept(res));
                screen.addRenderableWidget(button);
            }
            y.addAndGet(21);
        };

        paginator(poseStack, font, resources, renderItem, labelForNoItem, !hasRenderedButtons);
    }

    private void renderTitle(PoseStack poseStack, Component title, int y) {
        GuiHelper.drawCenteredString(poseStack, font, title, journalMidX, y, textColor);
    }

    private void redraw() {
        ClientHelper.getClient().ifPresent(mc -> screen.init(mc, screen.width, screen.height));
    }

    private String getTruncatedName(String name, int length) {
        return name.substring(0, Math.min(name.length(), length));
    }

    private void renderRunesString(PoseStack poseStack, String runes, int left, int top, int xOffset, int yOffset, int xMax, int yMax, boolean withShadow) {
        var client = ClientHelper.getClient().orElse(null);
        if (client == null) return;

        // Convert the input string according to the runes that the player knows.
        String revealed = RuneHelper.revealRunes(runes, Journal2Helper.getLearnedRunes());

        int index = 0;

        for (int y = 0; y < yMax; y++) {
            for (int x = 0; x < xMax; x++) {
                if (index < revealed.length()) {
                    Component rune;
                    int color;

                    String s = String.valueOf(revealed.charAt(index));
                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
                        rune = new TextComponent(KnowledgeHelper.UNKNOWN);
                        color = textColor;
                    } else {
                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
                        color = secondaryColor;
                    }

                    int xo = left + (x * xOffset);
                    int yo = top + (y * yOffset);

                    if (withShadow) {
                        client.font.drawShadow(poseStack, rune, xo, yo, color);
                    } else {
                        client.font.draw(poseStack, rune, xo, yo, color);
                    }
                }
                index++;
            }
        }
    }

    public enum JournalSection implements ICharmEnum {
        HOME,
        BOOKMARKS,
        BIOMES,
        STRUCTURES,
        DIMENSIONS
    }

    static {
        INCORRECT_DIMENSION = new TranslatableComponent("gui.strange.journal.incorrect_dimension");
    }
}
