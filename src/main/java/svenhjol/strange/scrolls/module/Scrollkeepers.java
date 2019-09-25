package svenhjol.strange.scrolls.module;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
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
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Scrollkeepers extends MesonModule
{
    public static final String SCROLLKEEPER = "scrollkeeper";
    public static final String ANONYMOUS = "3-1-4-1-5";
    public static final int MAX_LEVEL = 3;

    public static UUID ANY_SELLER = UUID.fromString(Scrollkeepers.ANONYMOUS);
    public static VillagerProfession profession;

    @Override
    public void init()
    {
        ImmutableSet<BlockState> states = ImmutableSet.copyOf(Scrolls.block.getStateContainer().getValidStates());
        PointOfInterestType type = new PointOfInterestType(SCROLLKEEPER, states, 1, null, 1);

        // TODO move to Meson
        Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(SCROLLKEEPER), type);
        RegistryHandler.addRegisterable(type, new ResourceLocation(SCROLLKEEPER));
        PointOfInterestType.func_221052_a(type); // SO FUCKING SHIT

        profession = new VillagerProfession(SCROLLKEEPER, type, ImmutableSet.of(), ImmutableSet.of());
        ResourceLocation res = new ResourceLocation(Strange.MOD_ID, SCROLLKEEPER);
        RegistryHandler.registerVillager(profession, res);
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
                if (quest.getCriteria().isSatisfied()) {
                    sellers.add(quest.getSeller());
                }
            });

            int range = 10;
            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;

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
            handInQuest(player, villager);
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event)
    {
        Int2ObjectMap<List<ITrade>> trades = event.getTrades();
        VillagerProfession profession = event.getType();

        if (profession.getRegistryName() == null || !profession.getRegistryName().getPath().equals(SCROLLKEEPER)) return;

        // add tiered scrolls for each villager trade level
        for (int tier = 1; tier < Scrolls.MAX_TIERS; tier++) {
            for (int j = 0; j < 3; j++) {
                trades.get(tier).add(new ScrollForEmeralds(tier));
            }
        }
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

    private void handInQuest(PlayerEntity player, VillagerEntity villager)
    {
        Map<UUID, List<IQuest>> sellers = new HashMap<>();
        IQuestsCapability cap = Quests.getCapability(player);
        UUID villagerId = villager.getUniqueID();
        List<IQuest> completableQuests = new ArrayList<>();

        for (IQuest quest : cap.getCurrentQuests(player)) {
            UUID seller = quest.getSeller();
            if (!quest.getCriteria().isSatisfied()) continue;
            if (!sellers.containsKey(seller)) sellers.put(seller, new ArrayList<>());
            sellers.get(seller).add(quest);
        }

        if (sellers.isEmpty()) return;

        if (sellers.containsKey(ANY_SELLER)) {
            completableQuests.addAll(sellers.get(ANY_SELLER));
        }

        if (sellers.containsKey(villagerId)) {
            completableQuests.addAll(sellers.get(villagerId));
        }

        if (!completableQuests.isEmpty()) {

            VillagerData data = villager.getVillagerData();
            int villagerXp = villager.getXp();
            int villagerLevel = data.getLevel();
            boolean levelUp = false;

            // complete all completable quests and get the highest quest tier to level up the villager
            int highestTier = 0;
            for (IQuest quest : completableQuests) {
                int questTier = quest.getTier();
                highestTier = Math.max(highestTier, questTier);

                if (questTier == villagerLevel) {
                    int newXp = villagerXp + (questTier * 4);
                    villager.setXp(newXp);
                    int newLevel = VillagerData.func_221127_c(newXp);
                    levelUp = newLevel > villagerLevel;
                    Meson.log(newXp, newLevel, villagerLevel);
                }

                MinecraftForge.EVENT_BUS.post(new QuestEvent.Complete(player, quest));
            }

            if (levelUp && villagerLevel < MAX_LEVEL) {
                if (player.world.rand.nextFloat() < 0.1 + (villagerLevel * 0.15)) {
                    EffectInstance badOmen = new EffectInstance(Effects.BAD_OMEN, 120000, Math.max(0, villagerLevel-2), false, false, true);
                    player.addPotionEffect(badOmen);
                }
            }

            Quests.update(player);
        }
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
            ItemStack in1 = new ItemStack(Items.EMERALD, 1 + (rand.nextInt(tier * 2)));
            ItemStack out = new ItemStack(Scrolls.tiers.get(tier), 1);

            IQuest quest = new Quest();
            quest.setTier(tier);
            quest.setSeller(merchant.getUniqueID());

            ScrollItem.putTag(out, quest.toNBT());
            out.setDisplayName(new StringTextComponent(I18n.format("item.strange.scroll_tier", tier)));
            return new MerchantOffer(in1, out, 3, 0, 0.2F);
        }
    }
}
