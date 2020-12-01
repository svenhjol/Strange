package svenhjol.strange.traveljournals.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import svenhjol.charm.Charm;
import svenhjol.strange.traveljournals.JournalEntry;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.traveljournals.TravelJournalsClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class UpdateEntryScreen extends BaseScreen {
    private JournalEntry entry;
    private TextFieldWidget nameField;
    private String name;

    private boolean atEntryPosition;
    private boolean hasScreenshot;
    private boolean hasMap;

    private boolean hasRenderedColorButtons = false;
    private boolean hasRenderedTrashButton = false;
    private boolean hasRenderedMapButton = false;

    private int buttonLeftOffset = 18;
    private int buttonRightOffset = 113;

    private final List<DyeColor> colors = Arrays.asList(
        DyeColor.BLACK, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.RED, DyeColor.BROWN, DyeColor.GREEN, DyeColor.GRAY
    );
    private NativeImageBackedTexture screenshotTexture = null;
    private Identifier registeredScreenshotTexture = null;
    private int screenshotFailureRetries = 0;

    public UpdateEntryScreen(JournalEntry entry) {
        super(entry.name);
        this.entry = entry.copy();
        this.name = entry.name;
        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();

        if (client == null || client.world == null || client.player == null || !client.world.isClient)
            return;

        // set up the input field for editing the entry name
        nameField = new TextFieldWidget(textRenderer, (width / 2) - 72, 34, 149, 12, new LiteralText("NameField"));
        nameField.changeFocus(true);
        nameField.setFocusUnlocked(false);
        nameField.setEditableColor(-1);
        nameField.setUneditableColor(-1);
        nameField.setHasBorder(true);
        nameField.setMaxLength(255);
        nameField.setChangedListener(this::responder);
        nameField.setText(this.name);
        nameField.setEditable(true);

        client.keyboard.setRepeatEvents(true);
        children.add(nameField);
        setFocused(nameField);


        // reset cached rendered items
        hasRenderedColorButtons = false;
        hasRenderedTrashButton = false;
        hasRenderedMapButton = false;


        // reset state
        hasMap = client.player.inventory.contains(new ItemStack(Items.MAP));
        atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(client.player, entry);
        hasScreenshot = hasScreenshot();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        int mid = width / 2;
        int top = 20;

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
            this.addButton(new ButtonWidget((width / 2) - 50, colorsTop - 30, 100, 20, new TranslatableText("gui.strange.travel_journal.new_screenshot"), button -> this.prepareScreenshot()));
        }

        // render color selection buttons
        if (!hasRenderedColorButtons) {
            for (int i = 0; i < colors.size(); i++) {
                final DyeColor col = colors.get(i);
                this.addButton(new TexturedButtonWidget(colorsLeft + (i * 22), colorsTop, 20, 18, (i * 20), 0, 18, COLORS, r -> setColor(col)));
            }
            hasRenderedColorButtons = true;
        }

        // render coordinates if in creative mode
        if (entry.pos != null) {
            if (client.player.isCreative())
                drawCenteredString(matrices, textRenderer, I18n.translate("gui.strange.travel_journal.entry_location", entry.pos.getX(), entry.pos.getZ(), entry.dim), (width / 2), coordsTop, TEXT_COLOR);
        }

        centredString(matrices, textRenderer, I18n.translate("gui.strange.travel_journal.update", this.name), width / 2, top, DyeColor.byId(entry.color).getSignColor());
        nameField.render(matrices, mouseX, mouseY, delta);


        /*
         * --- Buttons on left of page ---
         */

        top = 13;

        // button to delete entry
        if (!hasRenderedTrashButton) {
            this.addButton(new TexturedButtonWidget(mid - 127, top, 20, 18, 160, 0, 19, BUTTONS, r -> delete()));
            hasRenderedTrashButton = true;
        }


        /*
         * --- Buttons on right of page ---
         */

        top = 13;

        // button to make map
        if (!hasRenderedMapButton && hasMap) {
            this.addButton(new TexturedButtonWidget(mid + buttonRightOffset, top, 20, 18, 40, 0, 19, BUTTONS, button -> makeMap()));
            hasRenderedMapButton = true;
            top += buttonLeftOffset;
        }
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;
        final boolean atEntryPosition = TravelJournalsClient.isPlayerAtEntryPosition(client.player, entry);

        int buttonX = atEntryPosition ? -110 : -50;
        int buttonDist = 120;

        if (atEntryPosition) {
            this.addButton(new ButtonWidget((width / 2) + buttonX, y, w, h, new TranslatableText("gui.strange.travel_journal.new_screenshot"), (button) -> this.prepareScreenshot()));
            buttonX += buttonDist;
        }

        this.addButton(new ButtonWidget((width / 2) + buttonX, y, w, h, new TranslatableText("gui.strange.travel_journal.save"), (button) -> this.save()));
    }

    private void responder(String str) {
        this.entry.name = str;
    }

    private void setColor(DyeColor color) {
        this.entry.color = color.getId();
        saveProgress();
    }

    private void delete() {
        TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_DELETE_ENTRY, this.entry.toTag());
        this.backToMainScreen();
    }

    private void prepareScreenshot() {
        if (client != null && client.player != null) {
            client.openScreen(null);
            client.options.hudHidden = true;
            TravelJournalsClient.entryHavingScreenshot = this.entry;
            TravelJournalsClient.screenshotTicks = 1;
        }
    }

    private boolean hasScreenshot() {
        File screenshot = getScreenshot();
        return screenshot.exists();
    }

    private File getScreenshot() {
        File screenshotsDirectory = new File(client.runDirectory, "screenshots");
        return new File(screenshotsDirectory, this.entry.id + ".png");
    }

    private void makeMap() {
        TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_MAKE_MAP, this.entry.toTag());
        hasMap = false;
    }

    private void saveProgress() {
        TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_UPDATE_ENTRY, this.entry.toTag());
    }

    private void backToMainScreen() {
        if (client != null) {
            client.openScreen(null);
            TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_OPEN_JOURNAL, null);
        }
    }

    private void save() {
        this.saveProgress();
        this.backToMainScreen();
    }

    private void tryRenderScreenshot(MatrixStack matrices) {
        if (screenshotTexture == null) {
            try {
                File screenshotFile = getScreenshot();
                RandomAccessFile raf = new RandomAccessFile(screenshotFile, "r");
                if (raf != null)
                    raf.close();

                InputStream stream = new FileInputStream(screenshotFile);
                NativeImage screenshot = NativeImage.read(stream);
                screenshotTexture = new NativeImageBackedTexture(screenshot);
                registeredScreenshotTexture = client.getTextureManager().registerDynamicTexture("screenshot", screenshotTexture);
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
            client.getTextureManager().bindTexture(registeredScreenshotTexture);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.66F, 0.4F, 0.66F);
            this.drawTexture(matrices, (int)(( this.width / 2 ) / 0.66F) - 110, 130, 0, 0, 228, 200);
            GlStateManager.popMatrix();
        }
    }
}
