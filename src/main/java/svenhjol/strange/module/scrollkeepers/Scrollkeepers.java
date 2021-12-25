package svenhjol.strange.module.scrollkeepers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.VillagerHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.component.RewardComponent;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.runes.Tier;
import svenhjol.strange.module.scrollkeepers.ScrollkeeperTradeOffers.ScrollForEmeralds;
import svenhjol.strange.module.scrollkeepers.network.ServerReceiveCheckSatisfied;
import svenhjol.strange.module.scrollkeepers.network.ServerSendSatisfied;
import svenhjol.strange.module.writing_desks.WritingDesks;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, description = "Scrollkeepers sell quest scrolls and provide rewards on quest completion.")
public class Scrollkeepers extends CharmModule {
    public static String VILLAGER_ID = "strange_scrollkeeper";
    public static VillagerProfession SCROLLKEEPER;
    public static PoiType POIT;

    public static ServerSendSatisfied SERVER_SEND_SATISFIED;
    public static ServerReceiveCheckSatisfied SERVER_RECEIVE_CHECK_SATISFIED;

    public static final int[] QUEST_XP = new int[]{1, 10, 16, 24, 35, 44};

    @Config(name = "Bad Omen chance", description = "Chance (out of 1.0) of the player receiving Bad Omen when completing a quest with a scrollkeeper.\n" +
        "Bad Omen will never be applied if the scrollkeeper's level is less than Journeyman.")
    public static double badOmenChance = 0.05D;

    @Config(name = "Bad Omen difficulty scale", description = "If true, the difficulty of the raid will increase according to the scrollkeeper's level.")
    public static boolean scaleBadOmen = true;

    @Override
    public void register() {
        POIT = WorldHelper.addPointOfInterestType(WritingDesks.WRITING_DESK_BLOCK_ID, WritingDesks.WRITING_DESK, 1);
        SCROLLKEEPER = VillagerHelper.addProfession(VILLAGER_ID, POIT, SoundEvents.VILLAGER_WORK_LIBRARIAN);
        addDependencyCheck(mod -> Strange.LOADER.isEnabled(Quests.class) && Strange.LOADER.isEnabled(WritingDesks.class));
    }

    @Override
    public void runWhenEnabled() {
        SERVER_SEND_SATISFIED = new ServerSendSatisfied();
        SERVER_RECEIVE_CHECK_SATISFIED = new ServerReceiveCheckSatisfied();

        for (Tier tier : Tier.values()) {
            VillagerHelper.addTrade(SCROLLKEEPER, tier.getLevel(), new ScrollForEmeralds(tier));
        }
    }

    /**
     * Called from {@link svenhjol.strange.mixin.scrollkeepers.InteractWithVillagerMixin} when a player interacts with a villager.
     */
    public static boolean tryCompleteQuest(Player player, Villager villager) {
        Level level = player.level;
        if (level.isClientSide) return false;
        VillagerData villagerData = villager.getVillagerData();
        if (villagerData.getProfession() != SCROLLKEEPER) return false;

        // does the player have any satisfied quests?
        Optional<Quest> optQuest = QuestHelper.getFirstSatisfiedQuest(player);
        if (optQuest.isEmpty()) return false; // no finished quests
        Quest quest = optQuest.get();

        // complete the quest with the villager
        level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0F, 1.0F);
        quest.complete(player, villager);

        // handle villager xp increase and levelup
        int villagerXp = villager.getVillagerXp();
        int villagerLevel = villagerData.getLevel();
        int tier = quest.getTier().getLevel();
        int rewardXp = quest.getComponent(RewardComponent.class).getMerchantXp();

        if (tier >= villagerLevel) {
            int tierXp = QUEST_XP[Math.max(0, Math.min(Tier.values().length, tier) - 1)];
            if (tier > villagerLevel) {
                // scale down the bonus for completing a quest tier higher than the villager's level
                tierXp = Math.max(1, tierXp / 2);
            }
            int newVillagerXp = villagerXp + tierXp + rewardXp;
            villager.setVillagerXp(newVillagerXp);

            // trigger the villager levelup if has enough XP
            if (villager.shouldIncreaseLevel()) {
                villager.increaseMerchantCareer();
            }
        }

        // apply Bad Omen penalty
        if (badOmenChance > 0 && villagerLevel >= 3 && level.random.nextFloat() < Math.min(badOmenChance, 1.0D)) {
            int amplifier = scaleBadOmen ? Math.max(0, villagerLevel - 2) : 0;
            MobEffectInstance effect = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, amplifier, false, false, false);
            player.addEffect(effect);
            LogHelper.debug(Strange.MOD_ID, Scrollkeepers.class, "Applying Bad Omen effect with amplifier: " + amplifier);
        }

        return true;
    }
}
