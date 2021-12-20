package svenhjol.strange.module.journals2.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals2.screen.mini.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class MiniJournal {
    private final Screen screen;
    private Minecraft minecraft;
    private Font font;
    private ItemRenderer itemRenderer;

    public Section section;
    public Bookmark selectedBookmark;
    public ResourceLocation selectedBiome;
    public ResourceLocation selectedStructure;
    public ResourceLocation selectedDimension;
    public boolean hasRenderedButtons;
    public boolean hasFirstRendered;
    public int offset;
    public int midX;
    public int midY;
    public int journalMidX;

    public final int perPage;
    public final int textColor;
    public final int secondaryColor;
    public final int buttonWidth;
    public final int buttonHeight;

    public static final ResourceLocation CLOSED = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal.png");
    public static final ResourceLocation OPEN = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal_open.png");
    public static final Component INCORRECT_DIMENSION;

    private final Map<Section, BaseMiniScreen> childSections = new HashMap<>();

    public MiniJournal(Screen screen) {
        this.screen = screen;
        this.hasRenderedButtons = false;
        this.section = Section.HOME;
        this.offset = 0;
        this.perPage = 6;
        this.textColor = 0x222222;
        this.secondaryColor = 0x908080;
        this.buttonWidth = 88;
        this.buttonHeight = 20;

        this.childSections.put(Section.HOME, new MiniHomeScreen(this));
        this.childSections.put(Section.BOOKMARKS, new MiniBookmarksScreen(this));
        this.childSections.put(Section.BIOMES, new MiniBiomesScreen(this));
        this.childSections.put(Section.DIMENSIONS, new MiniDimensionsScreen(this));
        this.childSections.put(Section.STRUCTURES, new MiniStructuresScreen(this));
    }

    public void init(Minecraft minecraft) {
        this.minecraft = minecraft;

        midX = screen.width / 2;
        midY = screen.height / 2;
        journalMidX = midX - 88;

        if (screen.height % 2 == 0) {
            midY -= 1;
        }

        hasRenderedButtons = false;
        hasFirstRendered = false;

        //noinspection Convert2MethodRef
        getChildSection().ifPresent(child -> child.init());
    }

    /**
     * Resolve a section into a child screen using the childSections mapping.
     */
    public Optional<BaseMiniScreen> getChildSection() {
        return Optional.ofNullable(childSections.get(section));
    }

    public Screen getScreen() {
        return screen;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }

    public void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        ResourceLocation texture = section == Section.HOME ? CLOSED : OPEN;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        screen.blit(poseStack, (screen.width / 2) - 284, (screen.height / 2) - 144, 123, 222, 256, 256);
    }

    private void firstRender(PoseStack poseStack) {
        if (!hasFirstRendered) {

            getChildSection().ifPresent(child
                -> child.firstRender(poseStack, font, itemRenderer));

            hasFirstRendered = true;
        }
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY) {
        // We need to set the local references of the font and item renderers to those supplied by screen.
        font = screen.font;
        itemRenderer = screen.itemRenderer;

        firstRender(poseStack);

        getChildSection().ifPresent(child
            -> child.render(poseStack, itemRenderer, font));

//        switch (section) {
//            case HOME -> renderHome(poseStack, mouseX, mouseY);
//            case BOOKMARKS -> renderBookmarks(poseStack, mouseX, mouseY);
//            case BIOMES -> renderBiomes(poseStack, mouseX, mouseY);
//            case STRUCTURES -> renderStructures(poseStack, mouseX, mouseY);
//            case DIMENSIONS -> renderDimensions(poseStack, mouseX, mouseY);
//        }

        hasRenderedButtons = true;
    }

    public void changeSection(Section section) {
        this.section = section;
        redraw();
    }

    public void addBackButton(Button.OnPress onPress) {
        int buttonWidth = 74;
        int buttonHeight = 20;
        screen.addRenderableWidget(new Button(journalMidX - 38, midY + 76, buttonWidth, buttonHeight, JournalScreen.GO_BACK, onPress));
    }

//    public <T> void paginator(PoseStack poseStack, Font font, List<T> items, Consumer<T> renderItem, Supplier<Component> labelForNoItems, boolean shouldRenderButtons) {
//        int paginationY = midY + 50;
//        int currentPage = offset - 1;
//        List<T> sublist;
//
//        int size = items.size();
//        if (size > perPage) {
//            if (currentPage * perPage >= size || currentPage * perPage < 0) {
//                // out of range, reset
//                offset = 1;
//                currentPage = 0;
//            }
//            sublist = items.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, size));
//        } else {
//            sublist = items;
//        }
//
//        for (T item : sublist) {
//            renderItem.accept(item);
//        }
//
//        if (size > perPage) {
//            TranslatableComponent component = new TranslatableComponent("gui.strange.journal.page", offset);
//            GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, paginationY + 6, secondaryColor);
//
//            // only render pagination buttons on the first render pass
//            if (shouldRenderButtons) {
//                if (offset * perPage < size) {
//                    screen.addRenderableWidget(new ImageButton(midX - 64, paginationY, 20, 18, 120, 0, 18, JournalScreen.NAVIGATION, b -> {
//                        ++offset;
//                        redraw();
//                    }));
//                }
//                if (offset > 1) {
//                    screen.addRenderableWidget(new ImageButton(midX - 134, paginationY, 20, 18, 140, 0, 18, JournalScreen.NAVIGATION, b -> {
//                        --offset;
//                        redraw();
//                    }));
//                }
//            }
//        }
//
//        if (size == 0) {
//            GuiHelper.drawCenteredString(poseStack, font, labelForNoItems.get(), journalMidX, midY - 8, secondaryColor);
//        }
//    }

//    private void renderBookmarks(PoseStack poseStack, int mouseX, int mouseY) {
//        renderTitle(poseStack, JournalScreen.BOOKMARKS, midY - 94);
//
//        var bookmarks = BookmarksClient.branch;
//        if (bookmarks == null) return;
//
//        if (selectedBookmark != null) {
//            if (!DimensionHelper.isDimension(minecraft.level, selectedBookmark.getDimension())) return;
//
//            addBackButton(b -> {
//                selectedBookmark = null;
//                changeSection(Section.BOOKMARKS);
//            });
//
//            int y = midY - 78;
//            Component component = new TextComponent(getTruncatedName(selectedBookmark.getName(), 18));
//            itemRenderer.renderGuiItem(BookmarksClient.getBookmarkIconItem(selectedBookmark), journalMidX - 8, y);
//            GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, y + 20, textColor);
//            renderRunesString(poseStack, selectedBookmark.getRunes(), journalMidX - 46, midY - 8, 9, 14, 10, 4, false);
//        } else {
//            addBackButton(b -> changeSection(Section.HOME));
//
//            AtomicInteger y = new AtomicInteger(midY - 78);
//            Consumer<Bookmark> renderItem = bookmark -> {
//                TextComponent bookmarkName = new TextComponent(bookmark.getName());
//                String name = getTruncatedName(bookmark.getName(), 14);
//                itemRenderer.renderGuiItem(BookmarksClient.getBookmarkIconItem(bookmark), journalMidX - (buttonWidth / 2) - 12, y.get() + 2);
//
//                if (!hasRenderedButtons) {
//                    boolean correctDimension = DimensionHelper.isDimension(minecraft.level, bookmark.getDimension());
//
//                    // generate the button
//                    Button.OnTooltip tooltip = (b, p, i, j) -> screen.renderTooltip(p, correctDimension ? bookmarkName : INCORRECT_DIMENSION, i, j);
//                    Button.OnPress press = b -> {
//                        selectedBookmark = bookmark;
//                        redraw();
//                    };
//                    Button button = new Button(midX - (buttonWidth / 2) - 82, y.get(), buttonWidth, buttonHeight, new TextComponent(name), press, tooltip);
//
//                    if (!correctDimension) {
//                        button.active = false;
//                    }
//                    screen.addRenderableWidget(button);
//                }
//                y.addAndGet(21);
//            };
//            paginator(poseStack, font, bookmarks.values(), renderItem, () -> JournalScreen.NO_BOOKMARKS, !hasRenderedButtons);
//        }
//    }

//    private void renderBiomes(PoseStack poseStack, int mouseX, int mouseY) {
//        renderTitle(poseStack, JournalScreen.LEARNED_BIOMES, midY - 94);
//
//        var journal = Journals2Client.journal;
//        if (journal == null) return;
//
//        var biomes = Knowledge2Client.biomes;
//        if (biomes == null) return;
//
//        if (selectedBiome != null) {
//            addBackButton(b -> {
//                selectedBiome = null;
//                changeSection(Section.BIOMES);
//            });
//
//            var runes = biomes.get(selectedBiome);
//            if (runes == null) return;
//
//            renderResourceLocation(poseStack, selectedBiome, runes, minecraft, mouseX, mouseY);
//        } else {
//            addBackButton(b -> changeSection(Section.HOME));
//            renderResourceLocations(poseStack, journal.getLearnedBiomes(), () -> JournalScreen.NO_BIOMES, res -> {
//                selectedBiome = res;
//                redraw();
//            });
//        }
//    }
//
//    private void renderStructures(PoseStack poseStack, int mouseX, int mouseY) {
//        renderTitle(poseStack, JournalScreen.LEARNED_STRUCTURES, midY - 94);
//
//        var journal = Journals2Client.journal;
//        if (journal == null) return;
//
//        var structures = Knowledge2Client.structures;
//        if (structures == null) return;
//
//        if (selectedStructure != null) {
//            addBackButton(b -> {
//                selectedStructure = null;
//                changeSection(Section.STRUCTURES);
//            });
//
//            var runes = structures.get(selectedStructure);
//            if (runes == null) return;
//
//            renderResourceLocation(poseStack, selectedStructure, runes, minecraft, mouseX, mouseY);
//        } else {
//            addBackButton(b -> changeSection(Section.HOME));
//            renderResourceLocations(poseStack, journal.getLearnedStructures(), () -> JournalScreen.NO_STRUCTURES, res -> {
//                selectedStructure = res;
//                redraw();
//            });
//        }
//    }
//
//    private void renderDimensions(PoseStack poseStack, int mouseX, int mouseY) {
//        renderTitle(poseStack, JournalScreen.LEARNED_DIMENSIONS, midY - 94);
//
//        var journal = Journals2Client.journal;
//        if (journal == null) return;
//
//        var dimensions = Knowledge2Client.dimensions;
//        if (dimensions == null) return;
//
//        if (selectedDimension != null) {
//            addBackButton(b -> {
//                selectedDimension = null;
//                changeSection(Section.DIMENSIONS);
//            });
//
//            var runes = dimensions.get(selectedDimension);
//            if (runes == null) return;
//
//            renderResourceLocation(poseStack, selectedDimension, runes, minecraft, mouseX, mouseY);
//        } else {
//            addBackButton(b -> changeSection(Section.HOME));
//            renderResourceLocations(poseStack, journal.getLearnedDimensions(), () -> JournalScreen.NO_DIMENSIONS, res -> {
//                selectedDimension = res;
//                redraw();
//            });
//        }
//    }
//
//    private void renderResourceLocation(PoseStack poseStack, ResourceLocation resource, String runes, Minecraft minecraft, int mouseX, int mouseY) {
//        int y = midY - 78;
//        Component component = new TextComponent(getTruncatedName(StringHelper.snakeToPretty(resource.getPath(), true), 18));
//        GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, y + 20, textColor);
////        renderRunesString(poseStack, runes, journalMidX - 46, midY - 8, 9, 14, 10, 4, false);
//    }
//
//    private void renderResourceLocations(PoseStack poseStack, List<ResourceLocation> resources, Supplier<Component> labelForNoItem, Consumer<ResourceLocation> onPress) {
//        AtomicInteger y = new AtomicInteger(midY - 78);
//        Consumer<ResourceLocation> renderItem = res -> {
//            String name = getTruncatedName(StringHelper.snakeToPretty(res.getPath(), true), 16);
//            if (!hasRenderedButtons) {
//                Button button = new Button(midX - (buttonWidth / 2) - 96, y.get(), 100, buttonHeight, new TextComponent(name), b -> onPress.accept(res));
//                screen.addRenderableWidget(button);
//            }
//            y.addAndGet(21);
//        };
//
////        paginator(poseStack, font, resources, renderItem, labelForNoItem, !hasRenderedButtons);
//    }

    public void renderTitle(PoseStack poseStack, Component title, int y) {
        GuiHelper.drawCenteredString(poseStack, font, title, journalMidX, y, textColor);
    }

    public void redraw() {
        ClientHelper.getClient().ifPresent(mc -> screen.init(minecraft, screen.width, screen.height));
    }

//    private String getTruncatedName(String name, int length) {
//        return name.substring(0, Math.min(name.length(), length));
//    }

//    private void renderRunesString(PoseStack poseStack, String runes, int left, int top, int xOffset, int yOffset, int xMax, int yMax, boolean withShadow) {
//        var client = ClientHelper.getClient().orElse(null);
//        if (client == null) return;
//
//        // Convert the input string according to the runes that the player knows.
//        String revealed = RuneHelper.revealRunes(runes, Journal2Helper.getLearnedRunes());
//
//        int index = 0;
//
//        for (int y = 0; y < yMax; y++) {
//            for (int x = 0; x < xMax; x++) {
//                if (index < revealed.length()) {
//                    Component rune;
//                    int color;
//
//                    var unknown = String.valueOf(Runes.UNKNOWN_RUNE);
//                    String s = String.valueOf(revealed.charAt(index));
//                    if (s.equals(unknown)) {
//                        rune = new TextComponent(unknown);
//                        color = textColor;
//                    } else {
//                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
//                        color = secondaryColor;
//                    }
//
//                    int xo = left + (x * xOffset);
//                    int yo = top + (y * yOffset);
//
//                    if (withShadow) {
//                        client.font.drawShadow(poseStack, rune, xo, yo, color);
//                    } else {
//                        client.font.draw(poseStack, rune, xo, yo, color);
//                    }
//                }
//                index++;
//            }
//        }
//    }

    public enum Section implements ICharmEnum {
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
