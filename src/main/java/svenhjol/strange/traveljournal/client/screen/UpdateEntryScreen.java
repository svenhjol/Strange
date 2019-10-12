package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.message.ActionMessage;

public class UpdateEntryScreen extends BaseTravelJournalScreen
{
    private TextFieldWidget nameField;
    protected CompoundNBT entry;
    protected CompoundNBT updated;
    protected String id;

    public UpdateEntryScreen(String id, CompoundNBT entry, PlayerEntity player, Hand hand)
    {
        super(entry.getString(TravelJournalItem.NAME), player, hand);
        this.id = id;
        this.entry = entry;
        this.updated = entry.copy();
        this.passEvents = false;
    }

    @Override
    protected void init()
    {
        super.init();
        if (minecraft == null) return;

        minecraft.keyboardListener.enableRepeatEvents(true);
        nameField = new TextFieldWidget(font, (width / 2) - 50, 48, 103, 12, "NameField");
        nameField.setCanLoseFocus(false);
        nameField.changeFocus(true);
        nameField.setTextColor(-1);
        nameField.setDisabledTextColour(-1);
        nameField.setEnableBackgroundDrawing(true);
        nameField.setMaxStringLength(TravelJournalItem.MAX_NAME_LENGTH);
        nameField.func_212954_a(this::responder);
        nameField.setText(updated.getString(TravelJournalItem.NAME));
        nameField.setEnabled(true);
        children.add(nameField);
        setFocusedDefault(nameField);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground();
        renderBackgroundTexture();
        renderButtons();

        super.render(mouseX, mouseY, partialTicks);
        nameField.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed()
    {
        super.removed();
        this.minecraft.keyboardListener.enableRepeatEvents(false);
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
        int y = (height / 4) + 160;
        int w = 100;
        int h = 20;

        this.addButton(new Button((width / 2) - 110, y, w, h, I18n.format("gui.strange.travel_journal.save"), (button) -> this.save()));
        this.addButton(new Button((width / 2) + 10, y, w, h, I18n.format("gui.strange.travel_journal.cancel"), (button) -> this.cancel()));
    }

    private void save()
    {
        entry = updated;
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.UPDATE, id, hand, entry));
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    private void cancel()
    {
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    private void responder(String str)
    {
        updated.putString(TravelJournalItem.NAME, str);
    }
}
