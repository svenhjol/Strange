package svenhjol.strange.scrolls.client.toast;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.strange.scrolls.client.toast.QuestToastTypes.Type;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.module.Scrolls;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuestToast implements IToast
{
    private boolean hasPlayedSound;
    private IQuest quest;
    private String title;
    private String subTitle;
    private Type type;

    public QuestToast(IQuest quest, Type type, String title, String subTitle)
    {
        this.quest = quest;
        this.title = title;
        this.subTitle = subTitle;
        this.type = type;
    }

    public Visibility draw(ToastGui toastGui, long delta)
    {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);

        toastGui.blit(0, 0, 0, 0, 160, 32);

        List<String> lvt_5_1_ = toastGui.getMinecraft().fontRenderer.listFormattedStringToWidth("Really nope", 125);
        int lvt_6_1_ = 16746751;
        if (lvt_5_1_.size() == 1) {
            toastGui.getMinecraft().fontRenderer.drawString(I18n.format(title), 30.0F, 7.0F, lvt_6_1_ | -16777216);
            toastGui.getMinecraft().fontRenderer.drawString(I18n.format(subTitle), 30.0F, 18.0F, -1);
        } else {
            float lvt_8_1_ = 300.0F;
            int lvt_9_2_;
            if (delta < 1500L) {
                lvt_9_2_ = MathHelper.floor(MathHelper.clamp((float)(1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                toastGui.getMinecraft().fontRenderer.drawString(I18n.format(title), 30.0F, 11.0F, lvt_6_1_ | lvt_9_2_);
            } else {
                lvt_9_2_ = MathHelper.floor(MathHelper.clamp((float)(delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                int var10001 = lvt_5_1_.size();
                toastGui.getMinecraft().fontRenderer.getClass();
                int lvt_10_1_ = 16 - var10001 * 9 / 2;

                for (Iterator var11 = lvt_5_1_.iterator(); var11.hasNext(); lvt_10_1_ += 9) {
                    String lvt_12_1_ = (String)var11.next();
                    toastGui.getMinecraft().fontRenderer.drawString(lvt_12_1_, 30.0F, (float)lvt_10_1_, 16777215 | lvt_9_2_);
                    toastGui.getMinecraft().fontRenderer.getClass();
                }
            }
        }

        if (!this.hasPlayedSound && delta > 0L) {
            this.hasPlayedSound = true;
            if (type.equals(Type.Success)) {
                toastGui.getMinecraft().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
        }

        RenderHelper.enableGUIStandardItemLighting();
        toastGui.getMinecraft().getItemRenderer().renderItemAndEffectIntoGUI(null, ScrollItem.putTier(new ItemStack(Scrolls.item), quest.getTier()), 8, 8);
        return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
