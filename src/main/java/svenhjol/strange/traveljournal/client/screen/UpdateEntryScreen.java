package svenhjol.strange.traveljournal.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.module.TravelJournal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class UpdateEntryScreen extends BaseTravelJournalScreen {
    private TextFieldWidget nameField;
    protected String name;
    protected int color;
    protected Entry entry;
    protected String message = "";
    protected char[] runicName;
    protected final List<DyeColor> colors = Arrays.asList(
        DyeColor.BLACK, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.RED, DyeColor.BROWN, DyeColor.GREEN, DyeColor.LIGHT_GRAY
    );
    protected FontRenderer glyphs;
    protected File file = null;
    protected DynamicTexture tex = null;
    protected ResourceLocation res = null;
    protected boolean atEntryPosition;
    protected boolean hasScreenshot;
    protected boolean hasRenderedAddPhotoButton;
    protected boolean hasRenderedColorIcons;
    protected boolean hasRenderedTrashIcon;

    public UpdateEntryScreen(Entry entry, PlayerEntity player, Hand hand) {
        super(entry.name, player, hand);
        this.entry = entry;
        this.name = entry.name;
        this.color = entry.color > 0 ? entry.color : 15;
        this.passEvents = false;
        this.runicName = new char[]{};
    }

    @Override
    protected void init() {
        super.init();
        if (mc == null) return;
        if (!mc.world.isRemote) return;

        if (player.isCreative() || !Strange.client.discoveredRunes.isEmpty()) {
            this.glyphs = mc.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
            int dim = 128 + entry.dim;
            if (dim >= 0 && dim < 256) {
                String dimHex = Integer.toHexString(dim);

                if (dimHex.length() % 2 == 1)
                    dimHex = "0" + dimHex;

                // get first and second chars of dimHex
                String d0 = dimHex.substring(0, 1);
                String d1 = dimHex.substring(1, 2);

                // convert the entry pos to hex
                String posHex = Long.toHexString(entry.pos.toLong());
                posHex = StringUtils.leftPad(posHex, 4, "0");

                // interpolate dimHex within posHex
                String p0 = posHex.substring(0, 3);
                String p1 = posHex.substring(3);
                String hex = p0 + d0 + p1 + d1;

                StringBuilder assembled = new StringBuilder();

                char[] chars = hex.toCharArray();
                int rune = -1;
                boolean atLeastOneRune = false;

                for (int i = 0; i < chars.length; i++) {
                    String letter;
                    char c = chars[i];
                    int v = Character.getNumericValue(c);

                    if (i % 2 == 0) {
                        // first time round set the rune value
                        rune = v;
                    } else {
                        // second time round set the color and add the string
                        if (rune >= 0) {
                            final String color = Integer.toHexString(v);

                            if (player.isCreative() || Strange.client.discoveredRunes.contains(rune)) {
                                letter = RunestoneHelper.getRuneIntCharMap().get(rune).toString();
                                atLeastOneRune = true;
                            } else {
                                letter = "?";
                            }

                            assembled.append(letter);
                            assembled.append(color);
                            Meson.LOG.debug("(" + letter + color + ")");
                        }

                        rune = -1;
                    }
                }

                if (atLeastOneRune) {
                    this.runicName = assembled.toString().toCharArray();
                }
            }
        }

        mc.keyboardListener.enableRepeatEvents(true);
        nameField = new TextFieldWidget(font, (width / 2) - 72, 34, 149, 12, "NameField");
        nameField.setCanLoseFocus(false);
        nameField.changeFocus(true);
        nameField.setTextColor(-1);
        nameField.setDisabledTextColour(-1);
        nameField.setEnableBackgroundDrawing(true);
        nameField.setMaxStringLength(TravelJournalItem.MAX_NAME_LENGTH);
        nameField.setResponder(this::responder);
        nameField.setText(entry.name);
        nameField.setEnabled(true);
        children.add(nameField);
        setFocusedDefault(nameField);

        if (!mc.world.isRemote) return;
        file = getScreenshot(entry);

        atEntryPosition = isAtEntryPosition(player, entry);
        hasScreenshot = hasScreenshot();
        hasRenderedAddPhotoButton = false;
        hasRenderedColorIcons = false;
        hasRenderedTrashIcon = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        TravelJournal.client.closeIfNotHolding(mc, player, hand);

        int mid = this.width / 2;
        int y = 20;

        int colorsTopEdge;
        int coordsTopEdge;
        int colorsLeftEdge = mid - ((colors.size() * 21) / 2);

        if (hasScreenshot) {
            colorsTopEdge = y + 117;
            coordsTopEdge = y + 141;
        } else if (atEntryPosition) {
            colorsTopEdge = y + 64;
            coordsTopEdge = y + 91;
        } else {
            colorsTopEdge = y + 38;
            coordsTopEdge = y + 65;
        }

        renderBackgroundTexture();

        if (hasScreenshot) {
            if (tex == null) {
                try {
                    final RandomAccessFile raf = new RandomAccessFile(file, "r");
                    if (raf != null)
                        raf.close();

                    InputStream stream = new FileInputStream(file);
                    NativeImage screenshot = NativeImage.read(stream);
                    tex = new DynamicTexture(screenshot);
                    res = this.mc.getTextureManager().getDynamicTextureLocation("screenshot", tex);
                    stream.close();

                    if (tex == null || res == null) {
                        Strange.LOG.debug("Failed to load screenshot");
                    }
                } catch (Exception e) {
                    Strange.LOG.debug("Error loading screenshot: " + e);
                }
                Strange.LOG.debug("Loaded screenshot");
            }

            if (res != null) {
                mc.textureManager.bindTexture(res);
                GlStateManager.pushMatrix();
                GlStateManager.scalef(0.66F, 0.4F, 0.66F);
                this.blit( (int)(( this.width / 2 ) / 0.66F) - 110, 130, 0, 0, 228, 200);
                GlStateManager.popMatrix();
            }
        }

        // button to take photo
        if (atEntryPosition && !hasScreenshot && !hasRenderedAddPhotoButton) {
            hasRenderedAddPhotoButton = true;
            this.addButton(new Button((width / 2) - 73, y + 36, 152, 20, I18n.format("gui.strange.travel_journal.new_screenshot"), (button) -> this.prepareScreenshot()));
        }

        // generate color icons
        if (!hasRenderedColorIcons) {
            for (int i = 0; i < colors.size(); i++) {
                final DyeColor col = colors.get(i);
                this.addButton(new ImageButton(colorsLeftEdge + (i * 22), colorsTopEdge, 20, 18, (i * 20), 0, 18, COLORS, (r) -> setColor(col)));
            }
            hasRenderedColorIcons = true;
        }

        if (entry.pos != null) {
            // show the coordinates if in creative mode
            if (player.isCreative())
                this.drawCenteredString(this.font, I18n.format("gui.strange.travel_journal.entry_location", entry.pos.getX(), entry.pos.getZ(), entry.dim), (width / 2), coordsTopEdge, TEXT_COLOR);

            int offset = y - 2;
            String letter = null;


            if (runicName.length > 0) {
                // draw background for rune layout
                AbstractGui.fill(width / 2 + 113, y + 4, width / 2 + 149, y + (runicName.length * 8) + 4, 0x77000000);

                for (int j = 0; j < runicName.length; j++) {
                    if (j % 2 == 0) {
                        letter = String.valueOf(runicName[j]);
                    } else {
                        String colHex = String.valueOf(runicName[j]);
                        int colNum = Integer.parseUnsignedInt(colHex, 16);
                        boolean q = letter.equals("?");
                        FontRenderer f = (q ? this.font : this.glyphs);
                        offset += 14;

                        if (!q) {
                            ItemStack dyeItem = new ItemStack(DyeItem.getItem(DyeColor.byId(colNum)));
                            this.blitItemIcon(dyeItem, (width / 2) + 128, offset - 5);
                        }

                        this.drawCenteredString(f, letter, (width / 2) + 123, offset, q ? 0x888888 : 0xFFFFFF);
                    }
                }
            }
        }

        this.drawCenteredString(this.font, I18n.format("gui.strange.travel_journal.update", entry.name), (width / 2), y, DyeColor.byId(this.color).getColorValue());
        nameField.render(mouseX, mouseY, partialTicks);

        // button to delete entry
        if (!hasRenderedTrashIcon) {
            this.addButton(new ImageButton(mid - 128, y + 13, 20, 18, 80, 0, 19, BUTTONS, (r) -> delete()));
            hasRenderedTrashIcon = true;
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    protected void setColor(DyeColor color) {
        this.color = color.getId();
        saveProgress();
    }

    @Override
    public void removed() {
        super.removed();
        mc.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean keyPressed(int i1, int i2, int i3) {
        if (i1 == 256) player.closeScreen();
        return this.nameField.keyPressed(i1, i2, i3) || this.nameField.canWrite() || super.keyPressed(i1, i2, i3);
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;
        final boolean atEntryPosition = isAtEntryPosition(player, entry);

        int buttonX = atEntryPosition ? -110 : -50;
        int buttonDist = 120;

        if (atEntryPosition) {
            this.addButton(new Button((width / 2) + buttonX, y, w, h, I18n.format("gui.strange.travel_journal.new_screenshot"), (button) -> this.prepareScreenshot()));
            buttonX += buttonDist;
        }

        this.addButton(new Button((width / 2) + buttonX, y, w, h, I18n.format("gui.strange.travel_journal.save"), (button) -> this.save()));
    }

    private void saveProgress() {
        Entry updated = new Entry(this.entry);
        updated.name = this.name;
        updated.color = this.color;

        TravelJournalItem.updateEntry(player.getHeldItem(hand), updated);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.UPDATE, updated, hand));
        player.playSound(SoundEvents.ITEM_BOOK_PUT, 1.0F, 1.0F);

        this.entry = updated;
    }

    private void save() {
        this.saveProgress();
        this.back();
    }

    private void back() {
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    private void delete() {
        TravelJournalItem.deleteEntry(player.getHeldItem(hand), this.entry);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.DELETE, this.entry, hand));
        player.playSound(SoundEvents.BLOCK_WOOD_BREAK, 1.0F, 1.0F);
        this.back();
    }

    private File getScreenshot(Entry entry) {
        return new File(new File(Minecraft.getInstance().gameDir, "screenshots"), entry.id + ".png");
    }

    private boolean hasScreenshot() {
        File file = getScreenshot(entry);
        return file.exists();
    }

    private void responder(String str) {
        this.name = str;
    }

    private void prepareScreenshot() {
        mc.displayGuiScreen(null);
        mc.gameSettings.hideGUI = true;
        player.sendStatusMessage(new TranslationTextComponent("gui.strange.travel_journal.screenshot_in_progress"), true);
        Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.SCREENSHOT, entry, hand));
    }

    private boolean isAtEntryPosition(PlayerEntity player, Entry entry) {
        return TravelJournal.client.isPlayerAtEntryPosition(player, entry);
    }
}
