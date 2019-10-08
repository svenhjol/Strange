package svenhjol.strange.traveljournal.client.screen;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import svenhjol.meson.Meson;

public class UpdateEntryScreen extends BaseTravelJournalScreen
{
    private TextFieldWidget nameField;

    public UpdateEntryScreen(String id, String title, BlockPos pos, PlayerEntity player, Hand hand)
    {
        super(title, player, hand);
    }

    @Override
    protected void init()
    {
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.nameField = new TextFieldWidget(this.font, width / 2 + 62, 48, 103, 12, I18n.format("container.repair", new Object[0]));
        this.nameField.setCanLoseFocus(false);
        this.nameField.changeFocus(true);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(35);
        this.nameField.func_212954_a(this::responder);
        this.children.add(this.nameField);
        this.setFocusedDefault(this.nameField);
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
        return super.keyPressed(i1, i2, i3);
    }

    private void responder(String str)
    {
        if (!str.isEmpty()) {
            Meson.log(str);
        }
    }
}
