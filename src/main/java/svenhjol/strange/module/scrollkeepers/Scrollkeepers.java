package svenhjol.strange.module.scrollkeepers;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.SetupStructureCallback;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.VillagerHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.mixin.accessor.VillagerAccessor;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.scrolls.*;
import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.Optional;
import java.util.UUID;

import static svenhjol.charm.event.SetupStructureCallback.addVillageHouse;

@Module(mod = Strange.MOD_ID, client = ScrollKeepersClient.class, description = "Scrollkeepers are villagers that sell scrolls and accept completed quests. [Requires Scrolls]", alwaysEnabled = true)
public class Scrollkeepers extends CharmModule {
    public static String VILLAGER_ID = "strange_scrollkeeper";
    public static final int[] QUEST_XP = new int[]{1, 10, 16, 24, 35, 44};
    public static ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "writing_desk");
    public static WritingDeskBlock WRITING_DESK;

    public static final ResourceLocation MSG_SERVER_GET_SCROLL_QUEST = new ResourceLocation(Strange.MOD_ID, "server_quest_satisfied");
    public static final ResourceLocation MSG_CLIENT_RECEIVE_SCROLL_QUEST = new ResourceLocation(Strange.MOD_ID, "client_quest_satisfied");

    public static VillagerProfession SCROLLKEEPER;
    public static PoiType POIT;

    public static ScrollKeepersClient client;
    public static int interestRange = 16;

    @Config(name = "Bad Omen chance", description = "Chance (out of 1.0) of the player receiving Bad Omen when handing in a scroll.")
    public static double badOmenChance = 0.05D;

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        POIT = WorldHelper.addPointOfInterestType(BLOCK_ID, WRITING_DESK, 1);
        SCROLLKEEPER = VillagerHelper.addProfession(VILLAGER_ID, POIT, SoundEvents.VILLAGER_WORK_LIBRARIAN);
    }

    @Override
    public boolean depends() {
        return ModuleHandler.enabled("strange:scrolls");
    }

    @Override
    public void init() {
        // listen for entity interaction events
        UseEntityCallback.EVENT.register(this::tryHandInScroll);

        // register scrollkeeper trades
        VillagerHelper.addTrade(SCROLLKEEPER, 1, new ScrollkeeperTradeOffers.ScrollForEmeralds(1));
        VillagerHelper.addTrade(SCROLLKEEPER, 2, new ScrollkeeperTradeOffers.ScrollForEmeralds(2));
        VillagerHelper.addTrade(SCROLLKEEPER, 3, new ScrollkeeperTradeOffers.ScrollForEmeralds(3));
        VillagerHelper.addTrade(SCROLLKEEPER, 4, new ScrollkeeperTradeOffers.ScrollForEmeralds(4));
        VillagerHelper.addTrade(SCROLLKEEPER, 5, new ScrollkeeperTradeOffers.ScrollForEmeralds(5));

        // register scrollkeeper structures
        addVillageHouse(SetupStructureCallback.VillageType.PLAINS, new ResourceLocation("strange:village/plains/houses/plains_scrollkeeper"), 5);
        addVillageHouse(SetupStructureCallback.VillageType.SAVANNA, new ResourceLocation("strange:village/savanna/houses/savanna_scrollkeeper"), 5);
        addVillageHouse(SetupStructureCallback.VillageType.SNOWY, new ResourceLocation("strange:village/snowy/houses/snowy_scrollkeeper"), 5);
        addVillageHouse(SetupStructureCallback.VillageType.TAIGA, new ResourceLocation("strange:village/taiga/houses/taiga_scrollkeeper"), 5);
        addVillageHouse(SetupStructureCallback.VillageType.DESERT, new ResourceLocation("strange:village/desert/houses/desert_scrollkeeper"), 5);

        // listen for quest satisfied request coming from the client
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_GET_SCROLL_QUEST, this::handleGetScrollQuest);
    }

    public static void sendScrollQuestPacket(ServerPlayer player, Quest quest) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(quest.toTag());
        ServerPlayNetworking.send(player, MSG_CLIENT_RECEIVE_SCROLL_QUEST, buffer);
    }

    private InteractionResult tryHandInScroll(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (entity instanceof Villager) {
            ItemStack heldStack = player.getItemInHand(hand);
            Villager villager = (Villager)entity;

            if (villager.getVillagerData().getProfession() != SCROLLKEEPER)
                return InteractionResult.PASS;

            if (!(heldStack.getItem() instanceof ScrollItem))
                return InteractionResult.PASS;

            if (!world.isClientSide) {
                Optional<QuestManager> optionalQuestManager = Scrolls.getQuestManager();
                if (optionalQuestManager.isEmpty())
                    return InteractionResult.PASS;

                QuestManager questManager = optionalQuestManager.get();

                String questId = ScrollItem.getScrollQuest(heldStack);
                if (questId == null)
                    return InteractionResult.PASS;

                Optional<Quest> optionalQuest = questManager.getQuest(questId);
                if (optionalQuest.isEmpty()) {
                    ((VillagerAccessor)villager).invokeSetUnhappy();
                    return InteractionResult.FAIL;
                }

                Quest quest = optionalQuest.get();

                // must be the owner of the scroll
                UUID owner = ScrollItem.getScrollOwner(heldStack);
                if (owner != null && !player.getUUID().equals(owner)) {
                    ((VillagerAccessor)villager).invokeSetUnhappy();
                    return InteractionResult.FAIL;
                }

                // quest conditions haven't been satisfied yet
                if (!quest.isSatisfied(player)) {
                    ((VillagerAccessor)villager).invokeSetUnhappy();
                    return InteractionResult.FAIL;
                }

                // must be the merchant you bought the scroll from, or a scroll you found
                if (!villager.getUUID().equals(quest.getMerchant())
                    && !quest.getMerchant().equals(ScrollsHelper.ANY_UUID)) {
                    ((VillagerAccessor)villager).invokeSetUnhappy();
                    return InteractionResult.FAIL;
                }

                // success, tidy up the quest, give rewards etc.
                world.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
                quest.complete(player, villager);
                questManager.sendToast((ServerPlayer) player, quest, QuestToastType.Success, "event.strange.quests.completed");
                Scrolls.triggerCompletedScroll((ServerPlayer) player);
                heldStack.shrink(1);

                // handle villager xp increase and level-up
                VillagerData villagerData = villager.getVillagerData();
                int villagerXp = villager.getVillagerXp();
                int villagerLevel = villagerData.getLevel();
                int questTier = quest.getTier();
                int questRarity = quest.getRarity();

                if (questTier >= villagerLevel) {
                    int tierXp = QUEST_XP[Math.min(Scrolls.TIERS, questTier) - 1];
                    if (questTier > villagerLevel)
                        tierXp /= 2;

                    int rareXp = tierXp * questRarity;
                    int newVillagerXp = villagerXp + Math.max(tierXp, rareXp);
                    villager.setTradingPlayer(null);
                    villager.setVillagerXp(newVillagerXp);

                    if (((VillagerAccessor)villager).invokeShouldIncreaseLevel())
                        ((VillagerAccessor)villager).invokeIncreaseMerchantCareer();
                }

                // handle bad omen penalty
                if (badOmenChance > 0 && villagerLevel >= 3 && world.random.nextFloat() < (Math.min(badOmenChance, 1.0D) * (villagerLevel - 2))) {
                    int amplifier = Math.max(0, villagerLevel - 2);
                    MobEffectInstance badOmen = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, amplifier, false, false, true);
                    player.addEffect(badOmen);
                    Charm.LOG.debug("Applying bad omen of amplifier: " + amplifier);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void handleGetScrollQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        String questId = data.readUtf(4);

        server.execute(() -> {
            if (player == null)
                return;

            if (!Scrolls.getQuestManager().isPresent())
                return;

            QuestManager questManager = Scrolls.getQuestManager().get();
            if (!questManager.getQuest(questId).isPresent())
                return;

            Quest quest = questManager.getQuest(questId).get();
            Scrollkeepers.sendScrollQuestPacket(player, quest);
        });
    }
}
