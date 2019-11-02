package svenhjol.strange.scrolls.quest.condition;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.ServerBossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.charm.tools.item.BoundCompassItem;
import svenhjol.charm.tools.module.CompassBinding;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Encounter implements IDelegate
{
    public final static String ID = "Encounter";
    public ServerBossInfo bossInfo = new ServerBossInfo(new StringTextComponent(I18n.format("event.strange.quests.encounter")), BossInfo.Color.BLUE, BossInfo.Overlay.NOTCHED_10);

    private IQuest quest;
    private List<ResourceLocation> targets = new ArrayList<>();
    private Map<ResourceLocation, Integer> targetCount = new HashMap<>();
    private Map<ResourceLocation, Integer> targetHealth = new HashMap<>();
    private Map<ResourceLocation, List<String>> targetEffects = new HashMap<>();
    private int count;
    private int killed;
    private int totalHealth;
    private boolean spawned = false;
    private BlockPos location;

    private final String TARGETS = "targets";
    private final String TARGET_COUNT = "targetCount";
    private final String TARGET_HEALTH = "targetHealth";
    private final String TARGET_EFFECTS = "targetEffects";
    private final String COUNT = "count";
    private final String KILLED = "killed";
    private final String SPAWNED = "spawned";
    private final String LOCATION = "location";
    private final String TOTAL_HEALTH = "totalHealth";
    private final String QUEST_ID = "questId";

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (event instanceof QuestEvent.Accept) {
            final QuestEvent.Accept qe = (QuestEvent.Accept) event;
            return onStarted(qe.getQuest(), qe.getPlayer());
        }

        if (event instanceof QuestEvent.Complete || event instanceof QuestEvent.Fail || event instanceof QuestEvent.Decline) {
            return onEnded(((QuestEvent) event).getPlayer());
        }

        if (event instanceof PlayerTickEvent) {
            return onTick(((PlayerTickEvent)event).player);
        }

        if (event instanceof PlayerEvent.Clone) {
            return onDeath(((PlayerEvent.Clone) event).getPlayer());
        }

        if (event instanceof LivingDeathEvent) {
            return onMobKilled(((LivingDeathEvent) event).getEntityLiving());
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count != 0 && count <= killed;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int defeated = Math.min(this.killed, this.count);
        if (defeated == 0 || count == 0) return 0;
        return ((float)defeated / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT targetsTag = new CompoundNBT();
        CompoundNBT targetCountTag = new CompoundNBT();
        CompoundNBT targetHealthTag = new CompoundNBT();
        CompoundNBT targetEffectsTag = new CompoundNBT();

        // serialize targets
        if (!targets.isEmpty()) {
            int index = 0;
            for (ResourceLocation target : targets) {
                String s = target.toString();
                if (targets.size() > 0) targetsTag.putString(Integer.toString(index++), s);
                if (targetCount.size() > 0) targetCountTag.putInt(s, targetCount.get(target));
                if (targetHealth.size() > 0) targetHealthTag.putInt(s, targetHealth.get(target));
                if (targetEffects.size() > 0) {
                    CompoundNBT effectsTag = new CompoundNBT();
                    List<String> effects = targetEffects.get(target);
                    for (int i = 0; i < effects.size(); i++) {
                        effectsTag.putString(String.valueOf(i), effects.get(i));
                    }
                    targetEffectsTag.put(s, effectsTag);
                }
            }
        }

        CompoundNBT tag = new CompoundNBT();
        tag.put(TARGETS, targetsTag);
        tag.put(TARGET_COUNT, targetCountTag);
        tag.put(TARGET_HEALTH, targetHealthTag);
        tag.put(TARGET_EFFECTS, targetEffectsTag);
        tag.putInt(KILLED, killed);
        tag.putInt(COUNT, count);
        tag.putInt(TOTAL_HEALTH, totalHealth);
        tag.putBoolean(SPAWNED, spawned);
        tag.putLong(LOCATION, location != null ? location.toLong() : 0);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.count = data.getInt(COUNT);
        this.killed = data.getInt(KILLED);
        this.totalHealth = data.getInt(TOTAL_HEALTH);
        this.spawned = data.getBoolean(SPAWNED);
        this.location = BlockPos.fromLong(data.getLong(LOCATION));

        CompoundNBT targetsTag = (CompoundNBT)data.get(TARGETS);
        CompoundNBT targetCountTag = (CompoundNBT)data.get(TARGET_COUNT);
        CompoundNBT targetHealthTag = (CompoundNBT)data.get(TARGET_HEALTH);
        CompoundNBT targetEffectsTag = (CompoundNBT)data.get(TARGET_EFFECTS);

        this.targets = new ArrayList<>();
        this.targetCount = new HashMap<>();
        this.targetHealth = new HashMap<>();
        this.targetEffects = new HashMap<>();

        if (targetsTag != null && targetsTag.size() > 0) {
            for (int i = 0; i < targetsTag.size(); i++) {
                ResourceLocation target = ResourceLocation.tryCreate(targetsTag.getString(String.valueOf(i)));
                if (target == null) continue;
                String s = target.toString();

                this.targets.add(target);
                if (targetCountTag != null && targetCountTag.size() > 0) this.targetCount.put(target, targetCountTag.getInt(s));
                if (targetHealthTag != null && targetHealthTag.size() > 0) this.targetHealth.put(target, targetHealthTag.getInt(s));
                if (targetEffectsTag != null && targetEffectsTag.size() > 0) {
                    CompoundNBT effectsTag = (CompoundNBT)targetEffectsTag.get(s);
                    this.targetEffects.put(target, new ArrayList<>());
                    if (effectsTag != null && effectsTag.size() > 0) {
                        for (int j = 0; j < effectsTag.size(); j++) {
                            this.targetEffects.get(target).add(effectsTag.getString(String.valueOf(j)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Encounter addTarget(ResourceLocation target, int count, int health, List<String> effects)
    {
        this.targets.add(target);
        this.targetHealth.put(target, health);
        this.targetCount.put(target, count);
        this.targetEffects.put(target, effects);
        this.totalHealth += (health * count);
        return this;
    }

    public Encounter setCount(int count)
    {
        this.count = count;
        return this;
    }

    public int getKilled()
    {
        return this.killed;
    }

    public int getCount()
    {
        return this.count;
    }

    public List<ResourceLocation> getTargets()
    {
        return this.targets;
    }

    public void fail(PlayerEntity player)
    {
        Minecraft.getInstance().deferTask(() -> MinecraftForge.EVENT_BUS.post(new QuestEvent.Fail(player, quest)));
    }

    public boolean onStarted(IQuest quest, PlayerEntity player)
    {
        if (quest.getId().equals(this.quest.getId())) {
            Random rand = player.world.rand;
            int dist = 100;

            int x = -(dist/2) + rand.nextInt(dist);
            int z = -(dist/2) + rand.nextInt(dist);

            this.location = player.getPosition().add(x, 0, z);

            ItemStack compass = new ItemStack(CompassBinding.item);
            compass.setDisplayName(new StringTextComponent(quest.getTitle()));
            BoundCompassItem.setPos(compass, this.location);
            BoundCompassItem.setDim(compass, 0);
            Objects.requireNonNull(compass.getTag()).putString(QUEST_ID, quest.getId());
            player.addItemStackToInventory(compass);
            return true;
        }

        return false;
    }

    public boolean onEnded(PlayerEntity player)
    {
        World world = player.world;
        ImmutableList<NonNullList<ItemStack>> inventories = PlayerHelper.getInventories(player);

        // remove the quest helper equipment like compasses, maps, etc.  TODO probably should be quest helper method
        inventories.forEach(inv -> inv.forEach(stack -> {
            if (!stack.isEmpty() && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains(QUEST_ID)) {
                if (stack.getTag().getString(QUEST_ID).equals(quest.getId())) {
                    stack.shrink(1);
                }
            }
        }));

        WorldHelper.clearWeather(world);
        return true;
    }

    public boolean onMobKilled(LivingEntity killed)
    {
        if (killed.getEntityString() == null) return false;
        if (!killed.getTags().contains(quest.getId())) return false;
        this.killed++;

        if (isSatisfied()) WorldHelper.clearWeather(killed.world);
        return true;
    }

    public boolean onTick(PlayerEntity player)
    {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        boolean atLocation = WorldHelper.getDistanceSq(playerPos, this.location) < 8;

        if (this.spawned) {
            float remainingHealth = world.getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(24))
                .stream()
                .filter(m -> m.getTags().contains(quest.getId())) // only count the mobs that are part of the encounter
                .map(LivingEntity::getHealth) // get each mob's current health
                .reduce(0.0F, (total, health) -> total += health); // add each mob's current health together

            if (remainingHealth > 0 && totalHealth > 0) {
                bossInfo.setVisible(true);
                bossInfo.setPercent(remainingHealth / (float)totalHealth);
                bossInfo.addPlayer((ServerPlayerEntity) player);

                // TODO weather should scale with difficulty
                if (!world.getWorldInfo().isThundering() || !world.getWorldInfo().isRaining()) WorldHelper.stormyWeather(world);
                if (world.rand.nextFloat() < 0.05F) PlayerHelper.doLightningNearPlayer(player);
            } else {
                bossInfo.setVisible(false);
                bossInfo.removePlayer((ServerPlayerEntity) player);
            }
        }

        if (!this.spawned && atLocation) {
            int mobsSpawned = 0;
            int effectDuration = 10000; // something silly so it doesn't run out
            int effectAmplifier = 1; // TODO should scale with difficulty

            for (ResourceLocation target : targets) {
                Optional<EntityType<?>> type = EntityType.byKey(target.toString());
                if (!type.isPresent()) continue;

                int count = targetCount.get(target);
                int health = targetHealth.get(target);
                List<String> effects = targetEffects.get(target);

                for (int i = 0; i < count; i++) {
                    MobEntity mob = (MobEntity) type.get().create(world);
                    if (mob == null) continue;
                    mob.addTag(quest.getId());
                    boolean didSpawn = PlayerHelper.spawnEntityNearPlayer(player, mob, (m, pos) -> {
                        mob.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
                        mob.setHealth(health);
                        mob.enablePersistence();
                        effects.add("fire_resistance");
                        for (String effect : effects) {
                            Registry.EFFECTS.getValue(new ResourceLocation(effect)).ifPresent(value -> mob.addPotionEffect(new EffectInstance(value, effectDuration, effectAmplifier)));
                        }

                        ((ServerWorld) world).addLightningBolt(new LightningBoltEntity(world, (double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, true));
//                        world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F, false);
                    });
                    if (didSpawn) mobsSpawned++;
                }
            }

            if (mobsSpawned == 0) this.fail(player);
            WorldHelper.stormyWeather(world);
            setCount(mobsSpawned);
            this.spawned = true;
            return true;
        }

        return false;
    }

    public boolean onDeath(PlayerEntity player)
    {
        this.bossInfo.removePlayer((ServerPlayerEntity)player);
        return false;
    }
}