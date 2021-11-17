package svenhjol.strange.module.quests;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ClientModule(module = Quests.class)
public class QuestsClient extends CharmModule {
    public static final String DEFAULT_LOCALE = "en";
    public static QuestData quests;

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_SHOW_QUEST_TOAST, this::handleShowQuestToast);
        ClientPlayNetworking.registerGlobalReceiver(Quests.MSG_CLIENT_SYNC_PLAYER_QUESTS, this::handleSyncPlayerQuests);
    }

    public static void sendSyncQuests() {
        NetworkHelper.sendEmptyPacketToServer(Quests.MSG_SERVER_SYNC_PLAYER_QUESTS);
    }

    private void handleShowQuestToast(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        QuestToastType type = buffer.readEnum(QuestToastType.class);
        String definitionId = buffer.readUtf();
        int tier = buffer.readInt();

        client.execute(() -> client.getToasts().addToast(new QuestToast(type, definitionId, tier)));
    }

    private void handleSyncPlayerQuests(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
//        CompoundTag tag = buffer.readNbt();
//
//        client.execute(() -> {
//            currentQuests.clear();
//
//            if (tag != null && tag.contains("quest")) {
//                ListTag list = tag.getList("quest", 10);
//                for (Tag el : list) {
//                    Quest quest = new Quest((CompoundTag) el);
//                    currentQuests.add(quest);
//                }
//            }
//        });
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            quests = QuestData.fromNbt(tag);
        }
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
}
