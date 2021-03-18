package svenhjol.strange.scrolls;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QuestToast implements Toast {
    private boolean soundPlayed;
    private Quest quest;
    private String title;
    private String subTitle;
    private QuestToastType type;

    public QuestToast(Quest quest, QuestToastType type, String title, String subTitle) {
        this.quest = quest;
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());

        List<OrderedText> list = manager.getGame().textRenderer.wrapLines(new LiteralText(title), 125);
        int i = 16746751;

        if (list.size() == 1) {
            manager.getGame().textRenderer.draw(matrices, new LiteralText(title), 30.0F, 7.0F, i | -16777216);
            manager.getGame().textRenderer.draw(matrices, I18n.translate(subTitle), 30.0F, 18.0F, -1);
        } else {
            int l;
            if (startTime < 1500L) {
                l = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                manager.getGame().textRenderer.draw(matrices, new LiteralText(title), 30.0F, 11.0F, i | l);
            } else {
                l = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10000 = this.getHeight() / 2;
                int var10001 = list.size();
                manager.getGame().textRenderer.getClass();
                int m = var10000 - var10001 * 9 / 2;

                for(Iterator var12 = list.iterator(); var12.hasNext(); m += 9) {
                    OrderedText orderedText = (OrderedText)var12.next();
                    manager.getGame().textRenderer.draw(matrices, orderedText, 30.0F, (float)m, 16777215 | l);
                    manager.getGame().textRenderer.getClass();
                }
            }
        }

        if (!this.soundPlayed && startTime > 0L) {
            this.soundPlayed = true;
            if (type.equals(QuestToastType.Success)) {
                manager.getGame().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
        }

        manager.getGame().getItemRenderer().renderInGui(new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier())), 8, 8);
        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

}
