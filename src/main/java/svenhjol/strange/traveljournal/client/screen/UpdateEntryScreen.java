package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import svenhjol.meson.Meson;
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
        super("Change", player, hand);
        this.id = id;
        this.entry = entry;
        this.updated = entry.copy();
        this.passEvents = false;
    }

    @Override
    protected void init()
    {
        super.init();

        if (this.minecraft == null) return;

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.nameField = new TextFieldWidget(this.font, (width / 2) - 50, 48, 103, 12, "Face");
        this.nameField.setCanLoseFocus(false);
        this.nameField.changeFocus(true);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(true);
        this.nameField.setMaxStringLength(35);
        this.nameField.func_212954_a(this::responder);
        this.nameField.setText(updated.getString(TravelJournalItem.NAME));
        this.nameField.setEnabled(true);
        this.children.add(this.nameField);
        this.setFocusedDefault(this.nameField);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.renderBackgroundTexture();
        renderButtons();

        super.render(mouseX, mouseY, partialTicks);
        this.nameField.render(mouseX, mouseY, partialTicks);
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
//        return !this.nameField.keyPressed(i1, i2, i3) && !this.nameField.func_212955_f() ? super.keyPressed(i1, i2, i3) : true;
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
        this.close();
        entry = updated;
        PacketHandler.sendToServer(new ActionMessage(ActionMessage.UPDATE, id, hand, entry));
    }

    private void cancel()
    {
        mc.displayGuiScreen(new TravelJournalScreen(player, hand));
    }

    private void responder(String str)
    {
        if (!str.isEmpty()) {
            Meson.log(str);
            updated.putString(TravelJournalItem.NAME, str);
//            this.nameField.setText(str);
        }
    }
}
