package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestOffersScreen extends Screen {
    protected UUID villagerUuid;
    protected VillagerProfession villagerProfession;
    protected int villagerLevel;
    protected List<BaseQuestRenderer<?>> renderers = new ArrayList<>();
    protected int midX;

    public QuestOffersScreen(UUID villagerUuid, VillagerProfession villagerProfession, int villagerLevel) {
        super(QuestHelper.makeVillagerTitle(villagerProfession));
        this.villagerUuid = villagerUuid;
        this.villagerProfession = villagerProfession;
        this.villagerLevel = villagerLevel;

        var quests = Quests.VILLAGER_QUESTS.getOrDefault(villagerUuid, List.of());
        for (Quest<?> quest : quests) {
            var renderer = quest.type().makeRenderer(quest);
            renderers.add(renderer);
        }
    }

    @Override
    protected void init() {
        super.init();

        midX = width / 2;
        var yOffset = 40;

        for (BaseQuestRenderer<?> renderer : renderers) {
            renderer.initPagedOffer(this, yOffset);
            yOffset += renderer.getPagedOfferHeight();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX, 24);

        var yOffset = 40;
        for (int i = 0; i < renderers.size(); i++) {
            var renderer = renderers.get(i);
            renderer.renderPagedOffer(this, guiGraphics, yOffset, mouseX, mouseY);
            yOffset += renderer.getPagedOfferHeight();
        }
    }

    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        drawCenteredString(guiGraphics, getTitle(), x, y, 0xa05f50, false);
    }

    /**
     * Version of drawCenteredString that allows specifying of drop shadow.
     * @see GuiGraphics#drawCenteredString(Font, Component, int, int, int)
     * TODO: move to helper to avoid dupes
     */
    protected void drawCenteredString(GuiGraphics guiGraphics, Component component, int x, int y, int color, boolean dropShadow) {
        var formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, dropShadow);
    }
}
