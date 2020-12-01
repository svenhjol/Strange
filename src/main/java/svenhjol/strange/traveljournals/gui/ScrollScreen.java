package svenhjol.strange.traveljournals.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.traveljournals.TravelJournals;
import svenhjol.strange.traveljournals.TravelJournalsClient;

@SuppressWarnings("ConstantConditions")
public class ScrollScreen extends BaseScreen {
    private int titleTop = 15;

    public ScrollScreen() {
        super(I18n.translate("item.strange.travel_journal.scrolls"));
        this.passEvents = false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        TravelJournalsClient.closeIfNotHolding(this.client);

        if (!isClientValid())
            return;

        super.render(matrices, mouseX, mouseY, delta);

        PlayerEntity player = this.client.player;
    }

    @Override
    protected void renderButtons() {
        int y = (height / 4) + 140;
        int w = 100;
        int h = 20;

        this.addButton(new ButtonWidget((width / 2) - (w / 2), y, w, h, new TranslatableText("gui.strange.travel_journal.back"), button -> this.backToMainScreen()));
    }

    private void backToMainScreen() {
        if (client != null) {
            client.openScreen(null);
            TravelJournalsClient.sendServerPacket(TravelJournals.MSG_SERVER_OPEN_JOURNAL, null);
        }
    }
}
