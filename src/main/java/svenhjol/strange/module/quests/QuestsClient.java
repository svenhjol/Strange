package svenhjol.strange.module.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ClientModule(module = Quests.class)
public class QuestsClient extends CharmModule {
    public static List<Quest> quests = new ArrayList<>();

    public static ClientReceiveQuests CLIENT_RECEIVE_QUESTS;
    public static ClientReceiveQuestDefinitions CLIENT_RECEIVE_QUEST_DEFINITIONS;
    public static ClientReceiveQuestToast CLIENT_RECEIVE_QUEST_TOAST;
    public static ClientSendAbandonQuest CLIENT_SEND_ABANDON_QUEST;
    public static ClientSendPauseQuest CLIENT_SEND_PAUSE_QUEST;

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_QUESTS = new ClientReceiveQuests();
        CLIENT_RECEIVE_QUEST_DEFINITIONS = new ClientReceiveQuestDefinitions();
        CLIENT_RECEIVE_QUEST_TOAST = new ClientReceiveQuestToast();
        CLIENT_SEND_ABANDON_QUEST = new ClientSendAbandonQuest();
        CLIENT_SEND_PAUSE_QUEST = new ClientSendPauseQuest();
    }

    public static void renderIcon(Screen screen, PoseStack poseStack, int[] icon, int x, int y) {
        int w = QuestIcons.ICON_WIDTH;
        int h = QuestIcons.ICON_HEIGHT;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, QuestIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        screen.blit(poseStack, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public static String getTitle(QuestDefinition definition) {
        return getTranslatedKey(definition, "title");
    }

    public static String getDescription(QuestDefinition definition) {
        return getTranslatedKey(definition, "description");
    }

    public static String getHint(QuestDefinition definition) {
        return getTranslatedKey(definition, "hint");
    }

    public static String getTranslatedKey(QuestDefinition definition, String key) {
        var id = definition.getId();
        var lang = definition.getLang();

        if (lang == null) {
            return id;
        }

        var client = Minecraft.getInstance();
        var selected = client.getLanguageManager().getSelected();
        var code = selected.getCode();

        // split code, we only care about the first bit
        code = code.split("_")[0].toLowerCase(Locale.ROOT);

        if (!lang.containsKey(code)) {
            code = Quests.DEFAULT_LOCALE;
        }

        var value = lang.get(code).getOrDefault(key, id);
        return I18n.get(value);
    }
}
