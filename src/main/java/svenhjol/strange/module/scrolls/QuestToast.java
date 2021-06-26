package svenhjol.strange.module.scrolls;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QuestToast implements Toast {
    private boolean soundPlayed;
    private final Quest quest;
    private final String title;
    private final String subTitle;
    private final QuestToastType type;

    public QuestToast(Quest quest, QuestToastType type, String title, String subTitle) {
        this.quest = quest;
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
    }

    @Override
    public Visibility render(PoseStack pose, ToastComponent toast, long startTime) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        toast.blit(pose, 0, 0, 0, 0, this.width(), this.height());

        List<FormattedCharSequence> list = toast.getMinecraft().font.split(new TextComponent(title), 125);
        int i = 16746751;

        if (list.size() == 1) {
            toast.getMinecraft().font.draw(pose, new TextComponent(title), 30.0F, 7.0F, i | -16777216);
            toast.getMinecraft().font.draw(pose, I18n.get(subTitle), 30.0F, 18.0F, -1);
        } else {
            int l;
            if (startTime < 1500L) {
                l = Mth.floor(Mth.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                toast.getMinecraft().font.draw(pose, new TextComponent(title), 30.0F, 11.0F, i | l);
            } else {
                l = Mth.floor(Mth.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10000 = this.height() / 2;
                int var10001 = list.size();
                toast.getMinecraft().font.getClass();
                int m = var10000 - var10001 * 9 / 2;

                for(Iterator var12 = list.iterator(); var12.hasNext(); m += 9) {
                    FormattedCharSequence orderedText = (FormattedCharSequence)var12.next();
                    toast.getMinecraft().font.draw(pose, orderedText, 30.0F, (float)m, 16777215 | l);
                    toast.getMinecraft().font.getClass();
                }
            }
        }

        if (!this.soundPlayed && startTime > 0L) {
            this.soundPlayed = true;
            if (type.equals(QuestToastType.Success)) {
                toast.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
        }

        toast.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier())), 8, 8);
        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

}
