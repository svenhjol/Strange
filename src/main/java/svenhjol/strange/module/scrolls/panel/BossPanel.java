package svenhjol.strange.module.scrolls.panel;

import svenhjol.strange.init.StrangeIcons;
import svenhjol.strange.module.scrolls.tag.Boss;
import svenhjol.strange.module.scrolls.tag.Quest;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BossPanel extends BasePanel {
    public static BossPanel INSTANCE = new BossPanel();

    @Override
    public void render(Screen screen, PoseStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Boss boss = quest.getBoss();
        Map<ResourceLocation, Integer> entities = boss.getEntities();
        Map<ResourceLocation, Integer> killed = boss.getKilled();
        Map<ResourceLocation, Boolean> satisfied = boss.getSatisfied();
        Map<ResourceLocation, String> names = boss.getNames();

        if (entities.isEmpty())
            return;

        // panel title and icon
        TranslatableComponent titleText = new TranslatableComponent("gui.strange.scrolls.boss");
        drawString(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_SWORD, mid - 60, top - 1);

        top += rowHeight;

        // render out entities with name and quantity
        ArrayList<ResourceLocation> entityIds = new ArrayList<>(entities.keySet());

        // names
        int baseTop = top;
        for (ResourceLocation entityId : entityIds) {
            // remaining is count - killed
            int remaining = Math.max(0, entities.get(entityId) - killed.getOrDefault(entityId, 0));
            String name = names.get(entityId);
            TranslatableComponent text = new TranslatableComponent("gui.strange.scrolls.boss_entity", name, remaining);
            renderItemStack(new ItemStack(Items.DIAMOND_SWORD), mid - 60, baseTop - 5);
            drawString(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);

            // if all of this type is killed, show a tick next to it
            if (satisfied.containsKey(entityId) && satisfied.get(entityId))
                renderIcon(matrices, StrangeIcons.ICON_TICK, mid - 30 + getTextRenderer().width(text), baseTop - 1);

            baseTop += rowHeight;
        }
    }
}
