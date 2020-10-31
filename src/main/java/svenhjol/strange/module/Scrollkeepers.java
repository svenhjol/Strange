package svenhjol.strange.module;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestType;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.VillagerHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.mixin.accessor.VillagerEntityAccessor;
import svenhjol.strange.Strange;
import svenhjol.strange.scrollkeepers.ScrollKeepersClient;
import svenhjol.strange.scrolls.ScrollHelper;
import svenhjol.strange.scrolls.ScrollItem;
import svenhjol.strange.scrolls.tag.Quest;
import svenhjol.strange.scrollkeepers.ScrollkeeperTradeOffers;
import svenhjol.strange.writingdesks.WritingDesks;

@Module(mod = Strange.MOD_ID, description = "Scrollkeepers are villagers that sell scrolls and accept completed quests. [Requires Scrolls]", alwaysEnabled = true)
public class Scrollkeepers extends CharmModule {
    public static Identifier VILLAGER_ID = new Identifier(Strange.MOD_ID, "scrollkeeper");
    public static final int[] QUEST_XP = new int[]{1, 10, 16, 24, 35};

    public static VillagerProfession SCROLLKEEPER;
    public static PointOfInterestType POIT;

    public static ScrollKeepersClient client;
    public static int interestRange = 16;

    @Config(name = "Bad Omen chance", description = "Chance (out of 1.0) of the player receiving Bad Omen when handing in a scroll.")
    public static double badOmenChance = 0.03D;

    @Override
    public void register() {
        enabled = ModuleHandler.enabled("strange:scrolls");
    }

    public static void registerAfterWritingDesk() {
        POIT = VillagerHelper.addPointOfInterestType(WritingDesks.BLOCK_ID, WritingDesks.WRITING_DESK, 1);
        SCROLLKEEPER = VillagerHelper.addProfession(VILLAGER_ID, POIT, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);

        VillagerHelper.addTrade(SCROLLKEEPER, 1, new ScrollkeeperTradeOffers.ScrollForEmeralds(1));
        VillagerHelper.addTrade(SCROLLKEEPER, 2, new ScrollkeeperTradeOffers.ScrollForEmeralds(2));
        VillagerHelper.addTrade(SCROLLKEEPER, 3, new ScrollkeeperTradeOffers.ScrollForEmeralds(3));
        VillagerHelper.addTrade(SCROLLKEEPER, 4, new ScrollkeeperTradeOffers.ScrollForEmeralds(4));
        VillagerHelper.addTrade(SCROLLKEEPER, 5, new ScrollkeeperTradeOffers.ScrollForEmeralds(5));
    }

    @Override
    public void init() {
        UseEntityCallback.EVENT.register(this::tryHandInScroll);
    }

    @Override
    public void clientInit() {
        client = new ScrollKeepersClient(this);
        PlayerTickCallback.EVENT.register(player -> client.villagerInterested(player));
    }

    private ActionResult tryHandInScroll(PlayerEntity playerEntity, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (entity instanceof VillagerEntity) {
            ItemStack heldStack = playerEntity.getStackInHand(hand);
            VillagerEntity villager = (VillagerEntity)entity;
            if (!(heldStack.getItem() instanceof ScrollItem))
                return ActionResult.PASS;

            if (!world.isClient) {
                Quest quest = ScrollItem.getScrollQuest(heldStack);
                if (quest == null)
                    return ActionResult.PASS;

                // quest conditions haven't been satisfied yet
                if (!quest.isSatisfied(playerEntity)) {
                    ((VillagerEntityAccessor)villager).invokeSayNo();
                    return ActionResult.FAIL;
                }

                // must be the merchant you bought the scroll from, or a scroll you found
                if (!villager.getUuid().equals(quest.getMerchant())
                    && !quest.getMerchant().equals(ScrollHelper.ANY_UUID)) {
                    ((VillagerEntityAccessor)villager).invokeSayNo();
                    return ActionResult.FAIL;
                }

                // success, tidy up the quest, give rewards etc.
                world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.playSound(null, playerEntity.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                quest.complete(playerEntity, villager);
                heldStack.decrement(1);

                // handle villager xp increase and level-up
                VillagerData villagerData = villager.getVillagerData();
                int villagerXp = villager.getExperience();
                int villagerLevel = villagerData.getLevel();
                int questTier = quest.getTier();
                int questRarity = quest.getRarity();

                if (questTier >= villagerLevel) {
                    int tierXp = QUEST_XP[questTier - 1];
                    if (questTier > villagerLevel)
                        tierXp /= 2;

                    int rareXp = tierXp * questRarity;
                    int newVillagerXp = villagerXp + Math.max(tierXp, rareXp);
                    villager.setCurrentCustomer(null);
                    villager.setExperience(newVillagerXp);

                    if (((VillagerEntityAccessor)villager).invokeCanLevelUp())
                        ((VillagerEntityAccessor)villager).invokeLevelUp();
                }

                // handle bad omen penalty
                if (badOmenChance > 0 && villagerLevel >= 3 && world.random.nextFloat() < (Math.min(badOmenChance, 1.0D) * (villagerLevel - 2))) {
                    int amplifier = Math.max(0, villagerLevel - 2);
                    StatusEffectInstance badOmen = new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, amplifier, false, false, true);
                    playerEntity.addStatusEffect(badOmen);
                    Charm.LOG.debug("Applying bad omen of amplifier: " + amplifier);
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
