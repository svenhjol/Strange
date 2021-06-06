package svenhjol.strange.module.travel_journals.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import svenhjol.charm.Charm;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.mixin.accessor.ScreenAccessor;
import svenhjol.strange.module.travel_journals.TravelJournalsClient;
import svenhjol.strange.helper.NetworkHelper;
import svenhjol.strange.module.travel_journals.TravelJournals;
import svenhjol.strange.module.travel_journals.TravelJournalEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@SuppressWarnings("ConstantConditions")
public class TravelJournalUpdateEntryScreen extends TravelJournalBaseScreen {
    private TravelJournalEntry entry;
    private EditBox nameField;
    private String name;

    private boolean atEntryPosition;
    private boolean hasScreenshot;
    private boolean hasMap;
    private boolean hasTotem;

    private boolean hasRenderedColorButtons = false;
    private boolean hasRenderedTrashButton = false;
    private boolean hasRenderedMapButton = false;
    private boolean hasRenderedTotemButton = false;

    private final List<DyeColor> colors = Arrays.asList(
        DyeColor.BLACK, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.RED, DyeColor.BROWN, DyeColor.GREEN, DyeColor.GRAY
    );
    private DynamicTexture screenshotTexture = null;
    private ResourceLocation registeredScreenshotTexture = null;
    private int screenshotFailureRetries = 0;

    public TravelJournalUpdateEntryScreen(TravelJournalEntry entry) {
        super(entry.name);
        this.entry = entry.copy();
        this.name = entry.name;
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null || minecraft.level == null || minecraft.player == null || !minecraft.level.isClientSide)
            return;

        // set up the input field for editing the entry name
        nameField = new EditBox(font, (width / 2) - 72, 34, 149, 12, new TextComponent("NameField"));
        nameField.changeFocus(true);
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(true);
        nameField.setMaxLength(TravelJournals.MAX_NAME_LENGTH);
        nameField.setResponder(this::responder);
        nameField.setValue(this.name);
        nameField.setEditable(true);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        ((ScreenAccessor)this).getChildren().add(nameField);
        setFocused(nameField);

        // reset cached rendered items
        hasRenderedColorButtons = false;
        hasRenderedTrashButton = false;
        hasRenderedMapButton = false;

        // reset state
        hasMap = PlayerHelper.getInventory(minecraft.player).contains(new ItemStack(Items.MAP));
//        hasTotem = DimensionHelper.isDimension(client.world, entry.dim) && PlayerHelper.getInventory(client.player).contains(new ItemStack(TotemOfWandering.TOTEM_OF_WANDERING)); // TODO: phase2
        hasTotem = false;
        atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(minecraft.player, entry);
        hasScreenshot = hasScreenshot();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int mid = width / 2;
        int top = leftButtonYOffset;

        int colorsTop;
        int coordsTop;
        int colorsLeft = mid - ((colors.size() * 21) / 2);

        if (hasScreenshot) {
            colorsTop = top + 117;
            coordsTop = top + 141;
        } else if (atEntryPosition) {
            colorsTop = top + 64;
            coordsTop = top + 91;
        } else {
            colorsTop = top + 38;
            coordsTop = top + 65;
        }

        // render screenshot
        if (hasScreenshot) {
            tryRenderScreenshot(matrices);
        } else {
            this.addRenderableWidget(new Button((width / 2) - 50, colorsTop + 51, 100, 20, new TranslatableComponent("gui.strange.travel_journal.new_screenshot"), button -> this.prepareScreenshot()));
        }

        // render color selection buttons
        if (!hasRenderedColorButtons) {
            for (int i = 0; i < colors.size(); i++) {
                final DyeColor col = colors.get(i);
                this.addRenderableWidget(new ImageButton(colorsLeft + (i * 22), colorsTop, 20, 18, (i * 20), 0, 18, COLORS, r -> setColor(col)));
            }
            hasRenderedColorButtons = true;
        }

        // render coordinates if in creative mode or config allows it
        if (entry.pos != null) {
            if (minecraft.player.isCreative() || TravelJournals.showCoordinates) {
                Component coords = (new TextComponent("X:").append(String.valueOf(entry.pos.getX()))).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED))
                    .append((new TextComponent(" Z:").append(String.valueOf(entry.pos.getZ()))).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_BLUE)))
                    .append((new TextComponent(" ").append(StringHelper.capitalize(String.valueOf(entry.dim.getPath())))).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

                centeredText(matrices, font, coords, (width / 2), coordsTop, TEXT_COLOR);
            }
        }

        // render title and input field
        String title = I18n.get("gui.strange.travel_journal.update", this.name);
        centeredString(matrices, font, title.substring(0, Math.min(title.length(), NAME_CUTOFF)), width / 2, top, DyeColor.byId(entry.color).getTextColor());
        nameField.render(matrices, mouseX, mouseY, delta);

        this.renderNavigation(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;
        final boolean atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(minecraft.player, entry);

        int buttonX = atEntryPosition ? -110 : -50;
        int buttonDist = 120;

        if (atEntryPosition) {
            this.addRenderableWidget(new Button((width / 2) + buttonX, y, w, h, new TranslatableComponent("gui.strange.travel_journal.new_screenshot"), (button) -> this.prepareScreenshot()));
            buttonX += buttonDist;
        }

        this.addRenderableWidget(new Button((width / 2) + buttonX, y, w, h, new TranslatableComponent("gui.strange.travel_journal.save"), (button) -> this.save()));
    }

    private void responder(String str) {
        this.entry.name = str;
    }

    private void setColor(DyeColor color) {
        this.entry.color = color.getId();
        saveProgress();
    }

    private void delete() {
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_DELETE_ENTRY, buffer -> buffer.writeNbt(entry.toTag()));
        this.backToMainScreen();
    }

    private void prepareScreenshot() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.setScreen(null);
            minecraft.options.hideGui = true;
            TravelJournalsClient.entryHavingScreenshot = this.entry;
            TravelJournalsClient.screenshotTicks = 1;
        }
    }

    private boolean hasScreenshot() {
        File screenshot = getScreenshot();
        return screenshot.exists();
    }

    private File getScreenshot() {
        File screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, this.entry.id + ".png");
    }

    private void makeMap() {
        this.saveProgress();
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_MAKE_MAP, buffer -> buffer.writeNbt(entry.toTag()));
        init();
    }

    private void useTotem() {
        this.saveProgress();
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_USE_TOTEM, buffer -> buffer.writeNbt(entry.toTag()));
        init();
    }

    private void saveProgress() {
        NetworkHelper.sendPacketToServer(TravelJournals.MSG_SERVER_UPDATE_ENTRY, buffer -> buffer.writeNbt(entry.toTag()));
    }

    private void backToMainScreen() {
        if (minecraft != null)
            NetworkHelper.sendEmptyPacketToServer(TravelJournals.MSG_SERVER_OPEN_JOURNAL);
    }

    private void save() {
        this.saveProgress();
        this.backToMainScreen();
    }

    private void tryRenderScreenshot(PoseStack matrices) {
        if (screenshotTexture == null) {
            try {
                File screenshotFile = getScreenshot();
                RandomAccessFile raf = new RandomAccessFile(screenshotFile, "r");
                if (raf != null)
                    raf.close();

                InputStream stream = new FileInputStream(screenshotFile);
                NativeImage screenshot = NativeImage.read(stream);
                screenshotTexture = new DynamicTexture(screenshot);
                registeredScreenshotTexture = minecraft.getTextureManager().register("screenshot", screenshotTexture);
                stream.close();

                if (screenshotTexture == null || registeredScreenshotTexture == null)
                    Charm.LOG.debug("Null problems with screenshot texture / registered texture");

            } catch (Exception e) {
                Charm.LOG.debug("Error loading screenshot: " + e);
                screenshotFailureRetries++;

                if (screenshotFailureRetries > 4) {
                    Charm.LOG.warn("Failure loading screenshot, aborting retries");
                    hasScreenshot = false;
                    registeredScreenshotTexture = null;
                    screenshotTexture = null;
                }
            }
        }

        if (registeredScreenshotTexture != null) {
            RenderSystem.setShaderTexture(0, registeredScreenshotTexture);
            matrices.pushPose();
            matrices.scale(0.66F, 0.4F, 0.66F);
            this.blit(matrices, (int)(( this.width / 2 ) / 0.66F) - 110, 130, 0, 0, 228, 200);
            matrices.popPose();
        }
    }

    private void renderNavigation(PoseStack matrices, int mouseX, int mouseY, float delta) {
        int mid = width / 2;
        int top = leftButtonYOffset;

        // button to make map
        if (!hasRenderedMapButton && hasMap) {
            this.addRenderableWidget(new ImageButton(mid + leftButtonXOffset, top, 20, 18, 40, 0, 19, BUTTONS, button -> makeMap()));
            hasRenderedMapButton = true;
            top += rightButtonYOffset;
        }

        // button to use totem
        if (!hasRenderedTotemButton && hasTotem) {
            this.addRenderableWidget(new ImageButton(mid + leftButtonXOffset, top, 20, 18, 60, 0, 19, BUTTONS, button -> useTotem()));
            hasRenderedTotemButton = true;
            top += rightButtonYOffset;
        }

        // buttons at bottom
        top = 136;

        // button to delete entry
        if (!hasRenderedTrashButton) {
            this.addRenderableWidget(new ImageButton(mid + leftButtonXOffset, top, 20, 18, 20, 0, 19, BUTTONS, r -> delete()));
            hasRenderedTrashButton = true;
            top += leftButtonYOffset;
        }
    }
}
