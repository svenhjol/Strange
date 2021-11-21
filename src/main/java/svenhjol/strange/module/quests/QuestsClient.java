package svenhjol.strange.module.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@ClientModule(module = Quests.class)
public class QuestsClient extends CharmModule {
    public static final String DEFAULT_LOCALE = "en";
    public static QuestData quests;

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_SHOW_QUEST_TOAST, this::handleShowQuestToast);
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_SYNC_PLAYER_QUESTS, this::handleSyncPlayerQuests);
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_DESTROY_SCROLL, this::handleDestroyScroll);
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_OPEN_SCROLL, this::handleOpenScroll);
    }

    public static void sendSyncQuests() {
        NetworkHelper.sendEmptyPacketToServer(Quests.MSG_SERVER_SYNC_PLAYER_QUESTS);
    }

    public static void sendAbandonQuest(Quest quest) {
        NetworkHelper.sendPacketToServer(Quests.MSG_SERVER_ABANDON_QUEST, buf -> buf.writeUtf(quest.getId()));
    }

    public static void sendPauseQuest(Quest quest) {
        NetworkHelper.sendPacketToServer(Quests.MSG_SERVER_PAUSE_QUEST, buf -> buf.writeUtf(quest.getId()));
    }

    public static void renderIcon(Screen screen, PoseStack poseStack, int[] icon, int x, int y) {
        int w = QuestIcons.ICON_WIDTH;
        int h = QuestIcons.ICON_HEIGHT;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, QuestIcons.ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        screen.blit(poseStack, x, y, 256 - (icon[0] * w), icon[1] * h, w, h);
    }

    public static Optional<QuestData> getQuestData() {
        return Optional.ofNullable(quests);
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

    private static String getTranslatedKey(QuestDefinition definition, String key) {
        String definitionId = definition.getId();
        Map<String, Map<String, String>> langDefinition = definition.getLang();

        if (langDefinition == null) return definitionId;

        Minecraft client = Minecraft.getInstance();
        LanguageInfo selected = client.getLanguageManager().getSelected();
        String code = selected.getCode();

        // split code, we only care about the first bit
        code = code.split("_")[0].toLowerCase(Locale.ROOT);

        if (!langDefinition.containsKey(code)) {
            code = DEFAULT_LOCALE;
        }

        String value = langDefinition.get(code).getOrDefault(key, definitionId);
        return I18n.get(value);
    }

    private void handleShowQuestToast(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        QuestToastType type = buffer.readEnum(QuestToastType.class);
        String definitionId = buffer.readUtf();
        int tier = buffer.readInt();

        client.execute(() -> client.getToasts().addToast(new QuestToast(type, definitionId, tier)));
    }

    private void handleSyncPlayerQuests(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            quests = QuestData.fromNbt(tag);
        }
    }

    private void handleDestroyScroll(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        client.execute(() -> {
            Player player = client.player;
            if (player == null) return;

            double spread = 1.1D;
            Random random = player.level.random;
            for (int i = 0; i < 40; i++) {
                double px = player.blockPosition().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.blockPosition().getY() + 0.5D;
                double pz = player.blockPosition().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        });
    }

    private void handleOpenScroll(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        client.execute(() -> {
            Player player = client.player;
            if (player == null) return;

            double spread = 1.1D;
            Random random = player.level.random;
            for (int i = 0; i < 40; i++) {
                double px = player.blockPosition().getX() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                double py = player.blockPosition().getY() + 0.5D;
                double pz = player.blockPosition().getZ() + ((random.nextFloat()*2) - (random.nextFloat()*2)) * spread;
                player.level.addParticle(ParticleTypes.ENCHANT, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        });
    }
}
