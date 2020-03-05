package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.RunestoneHelper;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ServerTravelJournalAction;
import svenhjol.strange.traveljournal.module.TravelJournal;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class UpdateEntryScreen extends BaseTravelJournalScreen
{
    private TextFieldWidget nameField;
    protected String name;
    protected int color;
    protected Entry entry;
    protected String message = "";
    protected char[] runicName;
    protected List<DyeColor> colors = Arrays.asList(
        DyeColor.BLACK, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.RED, DyeColor.BROWN, DyeColor.GREEN, DyeColor.LIGHT_GRAY
    );
    protected FontRenderer glyphs;

    public UpdateEntryScreen(Entry entry, PlayerEntity player, Hand hand)
    {
        super(entry.name, player, hand);
        this.entry = entry;
        this.name = entry.name;
        this.color = entry.color > 0 ? entry.color : 15;
        this.passEvents = false;
        this.runicName = new char[] {};
    }

    @Override
    protected void init()
    {
        super.init();
        if (mc == null) return;
        if (!mc.world.isRemote) return;

        if (player.isCreative() || !Strange.client.discoveredRunes.isEmpty()) {
            this.glyphs = mc.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
            String hex = Long.toHexString(entry.pos.toLong());
            StringBuilder assembled = new StringBuilder();

            char[] chars = hex.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                String letter;
                char c = chars[i];
                int rune = Character.getNumericValue(c);

                if (player.isCreative() || Strange.client.discoveredRunes.contains(rune)) {
                    letter = RunestoneHelper.getRuneChars().get(c).toString();
                } else {
                    letter = "?";
                }
                assembled.append(letter);
            }
    //        assembled.append(Runestones.runeChars.get(Character.forDigit(entry.dim + 1, 10)).toString()); // only overworld for now
            this.runicName = assembled.toString().toCharArray();
        }

        mc.keyboardListener.enableRepeatEvents(true);
        nameField = new TextFieldWidget(font, (width / 2) - 50, 42, 103, 12, "NameField");
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
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        TravelJournal.client.closeIfNotHolding(mc, player, hand);
        boolean atEntryPosition = TravelJournal.client.isPlayerAtEntryPosition(player, entry);

        int mid = this.width / 2;
        int x = mid - 90;
        int y = 20;
        int colorsTopEdge = y + 46;
        int colorsLeftEdge = mid - ((colors.size() * 21) / 2);

        renderBackground();
        renderBackgroundTexture();
        renderButtons();

        this.drawCenteredString(this.font, I18n.format("gui.strange.travel_journal.update", entry.name), (width / 2), y + 8, DyeColor.byId(this.color).getColorValue());
        nameField.render(mouseX, mouseY, partialTicks);

        for (int i = 0; i < colors.size(); i++) {
            final DyeColor col = colors.get(i);
            this.addButton(new ImageButton(colorsLeftEdge + (i * 21), colorsTopEdge, 20, 18, (i * 20), 0, 18, COLORS, (r) -> setColor(col)));
        }

        if (entry.pos != null) {
            if (player.isCreative())
                this.drawCenteredString(this.font, I18n.format("gui.strange.travel_journal.entry_location", entry.pos.getX(), entry.pos.getZ(), entry.dim), (width / 2), y + 76, TEXT_COLOR);

            //this.drawCenteredString(this.glyphs, runicName, (width / 2), y + 94, TEXT_COLOR);
            int offset = y - 2;

            if (entry.dim == 0 && runicName.length > 0) {
                for (int j = 0; j < runicName.length; j++) {
                    String s = String.valueOf(runicName[j]);
                    FontRenderer f = (s.equals("?") ? this.font : this.glyphs);
                    int color = (s.equals("?")) ? 0xC0C0C0 : 0x888888;
                    this.drawCenteredString(f, s, (width / 2) + 93, offset + (j * 9), color);
                }
            }
        }

        if (atEntryPosition || hasScreenshot()) {
            String key = "gui.strange.travel_journal.show_screenshot";
            if (atEntryPosition && !hasScreenshot()) key = "gui.strange.travel_journal.new_screenshot";
            this.addButton(new Button((width / 2) - 60, y + 110, 120, 20, I18n.format(key), (button) -> this.screenshot()));
        }

        if (!this.message.isEmpty()) {
            this.drawCenteredString(this.font, this.message, (width / 2), y + 135, WARN_COLOR);
        }

        super.render(mouseX, mouseY, partialTicks);
    }

    protected void setColor(DyeColor color)
    {
        this.color = color.getId();
    }

    @Override
    public void removed()
    {
        super.removed();
        mc.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean keyPressed(int i1, int i2, int i3)
    {
        if (i1 == 256) player.closeScreen();
        return this.nameField.keyPressed(i1, i2, i3) || this.nameField.func_212955_f() || super.keyPressed(i1, i2, i3);
    }

    @Override
    protected void renderButtons()
    {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;
        int buttonX = -152;

        this.addButton(new Button((width / 2) + buttonX, y, w, h, I18n.format("gui.strange.travel_journal.save"), (button) -> this.save()));
        buttonX += 105;

        this.addButton(new Button((width / 2) + buttonX, y, w, h, I18n.format("gui.strange.travel_journal.delete"), (button) -> this.delete()));
        buttonX += 105;

        this.addButton(new Button((width / 2) + buttonX, y, w, h, I18n.format("gui.strange.travel_journal.cancel"), (button) -> this.back()));
    }

    private void saveProgress()
    {
        Entry updated = new Entry(this.entry);
        updated.name = this.name;
        updated.color = this.color;

        TravelJournalItem.updateEntry(player.getHeldItem(hand), updated);
        PacketHandler.sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.UPDATE, updated, hand));
        player.playSound(SoundEvents.ITEM_BOOK_PUT, 1.0F, 1.0F);

        this.entry = updated;
    }

    private void save()
    {
        this.saveProgress();
        this.back();
    }

    private void back()
    {
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    private void delete()
    {
        if (hasShiftDown()) {
            TravelJournalItem.deleteEntry(player.getHeldItem(hand), this.entry);
            PacketHandler.sendToServer(new ServerTravelJournalAction(ServerTravelJournalAction.DELETE, this.entry, hand));
            player.playSound(SoundEvents.BLOCK_WOOD_BREAK, 1.0F, 1.0F);
            this.back();
        } else {
            this.message = I18n.format("gui.strange.travel_journal.hold_shift_to_delete");
        }
    }

    private void screenshot()
    {
        this.saveProgress();
        TravelJournal.client.updateAfterScreenshot = true;
        mc.displayGuiScreen(new ScreenshotScreen(this.entry, player, hand));
    }

    private boolean hasScreenshot()
    {
        File file = ScreenshotScreen.getScreenshot(entry);
        return file.exists();
    }

    private void responder(String str)
    {
        this.name = str;
    }
}
