package svenhjol.strange.scrolls.module;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.generator.Generator;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Scrollkeepers extends MesonModule
{
    public static final String SCROLLKEEPER = "scrollkeeper";
    public static final String ANONYMOUS = "3-1-4-1-5";
    public static UUID ANY_SELLER = UUID.fromString(Scrollkeepers.ANONYMOUS);
    public static VillagerProfession profession;

    @Override
    public void init()
    {
        profession = new VillagerProfession(SCROLLKEEPER, PointOfInterestType.UNEMPLOYED, ImmutableSet.of(), ImmutableSet.of());
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, SCROLLKEEPER);
        RegistryHandler.registerVillager(profession, res);
    }

    @SubscribeEvent
    public void onVillagerSpawn(EntityJoinWorldEvent event)
    {
        if (!event.isCanceled()
            && !event.getWorld().isRemote
            && event.getEntity() instanceof VillagerEntity
        ) {
            VillagerEntity villager = (VillagerEntity)event.getEntity();
            VillagerData data = villager.getVillagerData();
            if (data.getProfession() == VillagerProfession.NONE) {
                villager.setVillagerData(data.withProfession(profession));
                villager.setXp(1);
                villager.populateTradeData(); // a hack
                Meson.debug("New scrollkeeper");
            }
        }
    }

    @SubscribeEvent
    public void onVillagerInterested(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && event.side == LogicalSide.CLIENT
            && event.player.world.isRemote
            && event.player.world.getGameTime() % 10 == 0
            && !QuestClient.currentQuests.isEmpty()
        ) {
            PlayerEntity player = event.player;
            World world = event.player.world;

            List<UUID> sellers = new ArrayList<>();
            QuestClient.currentQuests.forEach(quest -> {
                if (quest.getCriteria().isCompleted()) {
                    sellers.add(quest.getSeller());
                }
            });

            int range = 10;
            double x = event.player.posX;
            double y = event.player.posY;
            double z = event.player.posZ;

            List<VillagerEntity> villagers = world.getEntitiesWithinAABB(VillagerEntity.class, new AxisAlignedBB(
                x - range, y - range, z - range, x + range, y + range, z + range));

            villagers.forEach(villager -> {
                if (villager.getVillagerData().getProfession() == profession) {
                    if (sellers.contains(ANY_SELLER) || sellers.contains(villager.getUniqueID())) {
                        showInterest(villager);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onVillagerInteract(EntityInteractSpecific event)
    {
        if (!event.getPlayer().world.isRemote
            && event.getTarget() instanceof VillagerEntity
            && ((VillagerEntity)event.getTarget()).getVillagerData().getProfession() == profession
        ) {
            VillagerEntity villager = (VillagerEntity)event.getTarget();
            PlayerEntity player = event.getPlayer();
            boolean result = completeVillagerQuest(player, villager);
            // TODO reward
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event)
    {
        Int2ObjectMap<List<ITrade>> trades = event.getTrades();
        VillagerProfession profession = event.getType();

        if (profession.getRegistryName() == null || !profession.getRegistryName().getPath().equals(SCROLLKEEPER)) return;

        trades.get(1).add(new ScrollForEmeralds(1));
        trades.get(2).add(new ScrollForEmeralds(2));
        trades.get(3).add(new ScrollForEmeralds(3));
        trades.get(4).add(new ScrollForEmeralds(4));
        trades.get(5).add(new ScrollForEmeralds(5));
    }

    @OnlyIn(Dist.CLIENT)
    private void showInterest(VillagerEntity villager)
    {
        double spread = 0.5D;
        for (int i = 0; i < 3; i++) {
            double px = villager.getPosition().getX() + 0.25D + (Math.random() - 0.5D) * spread;
            double py = villager.getPosition().getY() + 2.25D + (Math.random() - 0.5D) * spread;
            double pz = villager.getPosition().getZ() + 0.25D + (Math.random() - 0.5D) * spread;
            villager.world.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.2D, 0.2D, 0.12D);
        }
    }

    private boolean completeVillagerQuest(PlayerEntity player, VillagerEntity villager)
    {
        Map<UUID, List<IQuest>> sellers = new HashMap<>();
        IQuestsCapability cap = Quests.getCapability(player);
        UUID villagerId = villager.getUniqueID();
        List<IQuest> completableQuests = new ArrayList<>();

        for (IQuest quest : cap.getCurrentQuests(player)) {
            UUID seller = quest.getSeller();
            if (!quest.getCriteria().isCompleted()) continue;
            if (!sellers.containsKey(seller)) sellers.put(seller, new ArrayList<>());
            sellers.get(seller).add(quest);
        }

        if (sellers.isEmpty()) return false;

        if (sellers.containsKey(ANY_SELLER)) {
            completableQuests.addAll(sellers.get(ANY_SELLER));
        }
        if (sellers.containsKey(villagerId)) {
            completableQuests.addAll(sellers.get(villagerId));
        }

        if (!completableQuests.isEmpty()) {
            // complete all completable quests and get the highest quest tier to level up the villager
            int highestTier = 0;
            for (IQuest quest : completableQuests) {
                highestTier = Math.max(highestTier, quest.getTier());
                MinecraftForge.EVENT_BUS.post(new QuestEvent.Complete(player, quest));
            }

            VillagerData data = villager.getVillagerData();
            int level = data.getLevel();

            if (level <= highestTier && level < 5) {
                // level up the villager
                int newLevel = level + 1;
                villager.setVillagerData(data.withLevel(newLevel));
                villager.populateTradeData();
                postLevelUp(player, villager, newLevel);
            }

            villager.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.75F, 1.0F);
            Quests.update(player);
            return true;
        }

//        disagree(villager);
        return false;
    }

    private void disagree(VillagerEntity villager)
    {
        villager.setShakeHeadTicks(40);
        if (!villager.world.isRemote) {
            villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, getPitch(villager));
        }
    }

    private void postLevelUp(PlayerEntity player, VillagerEntity villager, int newLevel)
    {
        boolean doRaid = player.world.rand.nextFloat() < (newLevel / 9.0F);
        villager.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0F, getPitch(villager));

        if (doRaid) {
            villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, getPitch(villager));
            EffectInstance badOmen = new EffectInstance(Effects.BAD_OMEN, 120000, newLevel-1, false, false, true);
            player.addPotionEffect(badOmen);
        }
    }

    private float getPitch(VillagerEntity villager)
    {
        Random rand = villager.world.rand;
        return villager.isChild() ? (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.5F : (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
    }

    static class ScrollForEmeralds implements ITrade
    {
        private final int tier;

        public ScrollForEmeralds(int tier)
        {
            this.tier = tier;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity merchant, Random rand)
        {
            ItemStack in1 = new ItemStack(Items.EMERALD, 3 + (rand.nextInt(tier * 3)));
            ItemStack out = new ItemStack(Scrolls.tiers.get(tier), 1);

            IQuest quest = Generator.generate(merchant.world, tier);
            quest.setSeller(merchant.getUniqueID());
            if (quest == null) return null;

            ScrollItem.putTag(out, quest.toNBT());
            out.setDisplayName(new StringTextComponent(quest.getTitle()));
            return new MerchantOffer(in1, out, 1, 0, 0.2F);
        }
    }
}
