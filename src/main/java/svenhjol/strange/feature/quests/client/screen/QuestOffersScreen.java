package svenhjol.strange.feature.quests.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestsClient;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.QuestsNetwork.AcceptQuest;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.helper.GuiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestOffersScreen extends Screen {
    protected UUID villagerUuid;
    protected VillagerProfession villagerProfession;
    protected int villagerLevel;
    protected List<BaseQuestRenderer<?>> renderers = new ArrayList<>();
    protected int midX;
    protected int titleColor;
    protected int loyaltyTextColor;

    public QuestOffersScreen(UUID villagerUuid, VillagerProfession villagerProfession, int villagerLevel) {
        super(QuestsHelper.makeVillagerOffersTitle(villagerProfession, villagerLevel));
        this.villagerUuid = villagerUuid;
        this.villagerProfession = villagerProfession;
        this.villagerLevel = villagerLevel;

        var quests = QuestsClient.getVillagerQuests(villagerUuid);
        for (Quest quest : quests.all()) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        midX = width / 2;

        for (var renderer : renderers) {
            var quest = renderer.quest();
            renderer.setAcceptAction(b -> AcceptQuest.send(quest.villagerUuid(), quest.id()));
            renderer.initPagedOffer(this);
        }

        titleColor = 0xd05f50;
        loyaltyTextColor = 0xe0c890;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX, 10);
        var yOffset = renderLoyalty(guiGraphics, mouseX, mouseY) ? 39 : 28;

        for (int i = 0; i < renderers.size(); i++) {
            var renderer = renderers.get(i);
            renderer.renderPagedOffer(guiGraphics, yOffset, mouseX, mouseY);
            yOffset += renderer.getPagedOfferHeight();
        }

        heartbeat();
    }

    protected boolean renderLoyalty(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var loyalty = QuestsClient.getLoyalty(villagerUuid);

        if (loyalty > 0) {
            var loyaltyString = Component.translatable(QuestsResources.LOYALTY_KEY, String.valueOf(loyalty));
            var width = font.width(loyaltyString);
            guiGraphics.drawString(font, loyaltyString, midX - (width / 2) - 3, 23, loyaltyTextColor, false);
            guiGraphics.blitSprite(QuestsResources.STAR, midX + (width / 2) - 1, 22, 9, 9);
        }

        return loyalty > 0;
    }

    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        GuiHelper.drawCenteredString(guiGraphics, font, getTitle(), x, y, titleColor, false);
    }

    protected void heartbeat() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getGameTime() % 60 != 0) {
            return;
        }

        if (minecraft.player != null) {
            QuestsClient.requestVillagerLoyalty(villagerUuid);
        }
    }
}
