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
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.knowledge.KnowledgeClientHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MiniJournalClient {
    private JournalSection section;

    private final Component standardTitle = new TranslatableComponent("gui.strange.journal.title");
    private final Component locationsTitle = new TranslatableComponent("gui.strange.journal.locations");
    private final Component biomesTitle = new TranslatableComponent("gui.strange.journal.learned_biomes");
    private final Component structuresTitle = new TranslatableComponent("gui.strange.journal.learned_structures");
    private final Component playersTitle = new TranslatableComponent("gui.strange.journal.learned_players");
    private final Component dimensionsTitle = new TranslatableComponent("gui.strange.journal.learned_dimensions");
    private final List<GuiHelper.ButtonDefinition> homeButtons = new ArrayList<>();
    private final Screen screen;

    private JournalLocation selectedLocation;
    private ResourceLocation selectedBiome;
    private ResourceLocation selectedStructure;
    private ResourceLocation selectedDimension;
    private UUID selectedPlayer;
    private boolean hasRenderedButtons;
    private int lastPage;
    private int midX;
    private int midY;

    public static final ResourceLocation CLOSED = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal.png");
    public static final ResourceLocation OPEN = new ResourceLocation(Strange.MOD_ID, "textures/gui/mini_journal_open.png");

    public MiniJournalClient(Screen screen) {
        this.screen = screen;
        this.hasRenderedButtons = false;
        this.section = JournalSection.HOME;
        this.lastPage = 0;

        // setup journal buttons
        this.homeButtons.addAll(Arrays.asList(
            new GuiHelper.ButtonDefinition(b -> changeJournalSection(JournalSection.LOCATIONS), new TranslatableComponent("gui.strange.journal.locations")),
            new GuiHelper.ButtonDefinition(b -> changeJournalSection(JournalSection.BIOMES), new TranslatableComponent("gui.strange.journal.learned_biomes")),
            new GuiHelper.ButtonDefinition(b -> changeJournalSection(JournalSection.STRUCTURES), new TranslatableComponent("gui.strange.journal.learned_structures")),
            new GuiHelper.ButtonDefinition(b -> changeJournalSection(JournalSection.PLAYERS), new TranslatableComponent("gui.strange.journal.learned_players")),
            new GuiHelper.ButtonDefinition(b -> changeJournalSection(JournalSection.DIMENSIONS), new TranslatableComponent("gui.strange.journal.learned_dimensions"))
        ));
    }

    public void init() {
        midX = screen.width / 2;
        midY = screen.height / 2;
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
        Minecraft minecraft = ClientHelper.getClient().get();

        Player player = minecraft.player;
        if (player == null) return;

        Font font = screen.font;
        ItemRenderer itemRenderer = screen.itemRenderer;

        int journalMidX = midX - 88;
        int width = screen.width;
        int height = screen.height;
        int titleTop;
        int perPage = 7;
        int buttonWidth = 84;
        int buttonHeight = 20;
        int primaryColor = 0x222222;
        int secondaryColor = 0x909090;
        Component title;

        switch (section) {
            case HOME -> {
                title = standardTitle;
                titleTop = -86;
                if (!hasRenderedButtons) {
                    int x = journalMidX - (buttonWidth / 2);
                    int y = midY - 60; // start rendering buttons from here
                    int xOffset = 0;
                    int yOffset = 24;
                    JournalHomeScreen.renderHome(screen, font, width, poseStack, homeButtons, buttonWidth, buttonHeight, x, y, xOffset, yOffset);
                    hasRenderedButtons = true;
                }
            }
            case LOCATIONS -> {
                title = locationsTitle;
                titleTop = -94;
                if (selectedLocation != null) {
                    if (!DimensionHelper.isDimension(player.level, selectedLocation.getDimension())) {
                        return;
                    }

                    int x = journalMidX - (buttonWidth / 2);
                    int y = midY - 78;
                    String runes = selectedLocation.getRunes();
                    ItemStack icon = selectedLocation.getIcon();
                    Component component = new TextComponent(selectedLocation.getName());

                    // render item icon
                    itemRenderer.renderGuiItem(icon, x - 12, y + 2);
                    GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, y + 6, primaryColor);
                    KnowledgeClientHelper.renderRunesString(minecraft, poseStack, runes, journalMidX - 42, y + 30, 11, 14, 8, 4, primaryColor, secondaryColor, false);

                } else {
                    int y = midY - 78;
                    int yOffset = 21;
                    int paginationY = 214;
                    int currentPage = lastPage - 1;
                    ResourceLocation navigation = BaseJournalScreen.NAVIGATION;
                    List<JournalLocation> locations = journal.getLocations();
                    List<JournalLocation> sublist;

                    int numberOfLocations = locations.size();
                    if (numberOfLocations > perPage) {
                        if (currentPage * perPage >= numberOfLocations || currentPage * perPage < 0) {
                            // out of range, reset
                            lastPage = 1;
                            currentPage = 0;
                        }
                        sublist = locations.subList(currentPage * perPage, Math.min(currentPage * perPage + perPage, numberOfLocations));
                    } else {
                        sublist = locations;
                    }

                    for (JournalLocation location : sublist) {
                        String name = getTruncatedName(location.getName());
                        ItemStack icon = location.getIcon();

                        // render item icons
                        itemRenderer.renderGuiItem(icon, journalMidX - (buttonWidth / 2) - 12, y + 2);

                        if (!hasRenderedButtons) {
                            Button button = new Button(midX - (buttonWidth / 2) - 82, y, buttonWidth, buttonHeight, new TextComponent(name), b -> {
                                selectedLocation = location;
                                redraw();
                            });
                            if (!DimensionHelper.isDimension(player.level, location.getDimension())) {
                                button.active = false;
                            }
                            screen.addRenderableWidget(button);
                        }

                        y += yOffset;
                    }

                    if (numberOfLocations > perPage) {
                        TranslatableComponent component = new TranslatableComponent("gui.strange.journal.page", lastPage);
                        GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, paginationY + 6, secondaryColor);

                        // only render pagination buttons on the first render pass
                        if (!hasRenderedButtons) {
                            if (lastPage * perPage < numberOfLocations) {
                                screen.addRenderableWidget(new ImageButton(midX - 60, paginationY, 20, 18, 120, 0, 18, navigation, b -> {
                                    ++lastPage;
                                    redraw();
                                }));
                            }
                            if (lastPage > 1) {
                                screen.addRenderableWidget(new ImageButton(midX - 138, paginationY, 20, 18, 140, 0, 18, navigation, b -> {
                                    --lastPage;
                                    redraw();
                                }));
                            }
                        }
                    }

                    if (numberOfLocations == 0) {
                        Component component = new TranslatableComponent("gui.strange.journal.no_locations");
                        GuiHelper.drawCenteredString(poseStack, font, component, journalMidX, midY - 8, secondaryColor);
                    }

                    hasRenderedButtons = true;
                }
            }
            case BIOMES -> {
                titleTop = -78;
                title = biomesTitle;
            }
            case STRUCTURES -> {
                titleTop = -78;
                title = structuresTitle;
            }
            case PLAYERS -> {
                titleTop = -78;
                title = playersTitle;
            }
            case DIMENSIONS -> {
                titleTop = -78;
                title = dimensionsTitle;
            }
            default -> {
                titleTop = -78;
                title = standardTitle;
            }
        }

        // draw the journal title
        GuiHelper.drawCenteredString(poseStack, font, title, journalMidX, midY + titleTop, primaryColor);
    }

    private void changeJournalSection(JournalSection section) {
        this.section = section;
        redraw();
    }

    private void redraw() {
        ClientHelper.getClient().ifPresent(mc -> screen.init(mc, screen.width, screen.height));
    }

    private String getTruncatedName(String name) {
        int nameCutoff = 20;
        return name.substring(0, Math.min(name.length(), nameCutoff));
    }

    public enum JournalSection implements ICharmEnum {
        HOME,
        LOCATIONS,
        BIOMES,
        STRUCTURES,
        PLAYERS,
        DIMENSIONS
    }
}
