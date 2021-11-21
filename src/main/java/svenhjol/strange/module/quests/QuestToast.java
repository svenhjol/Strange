package svenhjol.strange.module.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.strange.Strange;
import svenhjol.strange.module.scrolls.Scrolls;

import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class QuestToast implements Toast {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/adventure.png");
    private final QuestToastType type;
    private final DisplayInfo displayInfo;
    private boolean playedSound;

    public QuestToast(QuestToastType type, String definitionId, int tier) {
        ItemStack stack;

        if (Strange.LOADER.isEnabled("scrolls")) {
            stack = new ItemStack(Scrolls.SCROLLS.get(tier));
        } else {
            stack = new ItemStack(Items.PAPER);
        }

        Component title;
        Component description = new TextComponent(I18n.get("gui.strange.quests." + type.getSerializedName().toLowerCase(Locale.ROOT)));

        if (Quests.DEFINITIONS.containsKey(tier) && Quests.DEFINITIONS.get(tier).containsKey(definitionId)) {
            QuestDefinition definition = Quests.DEFINITIONS.get(tier).get(definitionId);
            title = new TextComponent(QuestsClient.getTitle(definition));
        } else {
            title = new TextComponent(definitionId);
        }

        this.type = type;
        this.displayInfo = new DisplayInfo(stack, title, description, BACKGROUND, FrameType.CHALLENGE, true, false, false);
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
        // copypasta from AdvancementToast#render
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());
        List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(displayInfo.getTitle(), 125);
        if (list.size() == 1) {
            toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getDescription(), 30.0f, 7.0f, type.getTitleColor());
            toastComponent.getMinecraft().font.draw(poseStack, list.get(0), 30.0f, 18.0f, type.getDescriptionColor());
        } else {
            if (l < 1500L) {
                int k = Mth.floor(Mth.clamp((float)(1500L - l) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
                toastComponent.getMinecraft().font.draw(poseStack, displayInfo.getDescription(), 30.0f, 11.0f, k);
            } else {
                int k = Mth.floor(Mth.clamp((float)(l - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
                int m = this.height() / 2 - list.size() * toastComponent.getMinecraft().font.lineHeight / 2;
                for (FormattedCharSequence formattedCharSequence : list) {
                    toastComponent.getMinecraft().font.draw(poseStack, formattedCharSequence, 30.0f, (float)m, 0xFFFFFF | k);
                    m += toastComponent.getMinecraft().font.lineHeight;
                }
            }
        }

        if (type == QuestToastType.COMPLETED) {
            if (!this.playedSound && l > 0L) {
                this.playedSound = true;
                toastComponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f));
            }
        }

        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(displayInfo.getIcon(), 8, 8);
        return l >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public enum QuestToastType implements ICharmEnum {
        STARTED(0xFFFFFF, 0xFFFF00),
        ABANDONED(0xFFFFFF, 0x999999),
        COMPLETED(0xFFFFFF, 0xFF88FF);

        private final int titleColor;
        private final int descriptionColor;

        QuestToastType(int titleColor, int descriptionColor) {
            this.titleColor = titleColor;
            this.descriptionColor = descriptionColor;
        }

        public int getTitleColor() {
            return titleColor;
        }

        public int getDescriptionColor() {
            return descriptionColor;
        }
    }
}
