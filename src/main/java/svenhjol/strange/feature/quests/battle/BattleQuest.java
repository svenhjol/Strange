package svenhjol.strange.feature.quests.battle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.MobHelper;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BattleQuest extends Quest {
    static final String QUEST_PREFIX = "strange_battle_";
    static final String UID_PREFIX = "strange_uid_";
    static final String REQUIRED_KILLS_TAG = "required";
    static final String TICKS_TAG = "ticks";
    static final String HAS_SPAWNED_TAG = "has_spawned";
    static final String MAX_HEALTH_TAG = "max_health";

    final List<BattleTarget> targets = new ArrayList<>();
    int ticks = 0;
    float maxHealth = 0f; // Store in NBT
    float health = 0f; // This is just a transient tracker
    boolean hasSpawned = false;
    ServerBossEvent bossEvent;

    @Override
    public List<? extends Requirement> requirements() {
        return targets;
    }

    @Override
    public void tick(Player player) {
        super.tick(player);

        if (player.level().isClientSide()) {
            return;
        }

        var serverPlayer = (ServerPlayer)player;
        var serverLevel = (ServerLevel)serverPlayer.level();

        ticks++;

        if (satisfied()) {
            // Quest remains valid, exit early.
            stopEffects();
            return;
        }

        if (ticks > 100 && !hasSpawned && isInVillage(serverPlayer)) {
            for (var target : targets) {
                var result = target.trySpawn();
                if (!result) {
                    cleanup(id(), serverPlayer);

                    // Invalidate this quest.
                    cancel();
                    return;
                }
            }
            hasSpawned = true;
            for (var mob : getQuestMobs(id(), serverPlayer)) {
                maxHealth += mob.getMaxHealth();
            }
            health = maxHealth;
        }

        if (hasSpawned) {
            startEffects(serverPlayer);

            // Sample rather than check every tick.
            if (serverLevel.getGameTime() % 10 == 0) {
                health = 0f;
                for (var mob : getQuestMobs(id(), serverPlayer)) {
                    health += mob.getHealth();
                }
            }

            var progress = maxHealth > 0f ? health / maxHealth : 0f;
            bossEvent.setProgress(progress);
        }
    }

    @Override
    public void entityKilled(LivingEntity entity, DamageSource source) {
        if (entity == player && !satisfied()) {
            cancel();
        }

        targets.forEach(t -> t.entityKilled(entity, source));
    }

    @Override
    public void entityLeave(Entity entity) {
        if (entity == player && !satisfied()) {
            cancel();
        }

        targets.forEach(t -> t.entityLeave(entity));
    }

    @Override
    public void cancel() {
        super.cancel();
        stopEffects();
        cleanup(id(), player);
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        targets.clear();
        var list = tag.getList(REQUIRED_KILLS_TAG, 10);
        for (var t : list) {
            var target = new BattleTarget();
            target.load((CompoundTag)t);
            targets.add(target);
        }

        ticks = tag.getInt(TICKS_TAG);
        hasSpawned = tag.getBoolean(HAS_SPAWNED_TAG);
        maxHealth = tag.getFloat(MAX_HEALTH_TAG);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        var list = new ListTag();
        for (var target : targets) {
            var t = new CompoundTag();
            target.save(t);
            list.add(t);
        }
        tag.put(REQUIRED_KILLS_TAG, list);
        tag.putInt(TICKS_TAG, ticks);
        tag.putBoolean(HAS_SPAWNED_TAG, hasSpawned);
        tag.putFloat(MAX_HEALTH_TAG, maxHealth);
    }

    @Override
    protected void makeRequirements(QuestDefinition definition) {
        var battle = definition.battle().take(random());
        var effects = battle.effects();
        var mobs = battle.mobs();

        for (var mob : mobs) {
            this.targets.add(new BattleTarget(mob, effects));
        }
    }

    private boolean isInVillage(ServerPlayer player) {
        var pos = player.blockPosition();
        var level = (ServerLevel)player.level();
        return level.isVillage(pos);
    }

    private void startEffects(ServerPlayer player) {
        if (bossEvent == null) {
            bossEvent = (ServerBossEvent) new ServerBossEvent(QuestsResources.BATTLE_TEXT, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS)
                .setDarkenScreen(true);

            bossEvent.addPlayer(player);
        }
    }

    private void stopEffects() {
        if (bossEvent != null) {
            bossEvent.removeAllPlayers();
            bossEvent = null;
        }
    }

    public static void cleanup(String questId, Player player) {
        for (var entity : getQuestMobs(questId, player)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public static boolean hasQuestTag(String questId, Entity entity) {
        for (var tag : entity.getTags()) {
            if (tag.equals(QUEST_PREFIX + questId)) {
                return true;
            }
        }
        return false;
    }

    public static List<LivingEntity> getQuestMobs(String questId, Player player) {
        return player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(32.0d))
            .stream().filter(e -> hasQuestTag(questId, e)).toList();
    }

    private Optional<BlockPos> findRandomSpawnPos(EntityType<?> entity, ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            var x = pos.getX() + random().nextInt(24) - 8;
            var z = pos.getZ() + random().nextInt(24) - 8;
            var y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            var p = new BlockPos(x, y, z).below();
            var s = level.getBlockState(p);
            if (s.isValidSpawn(level, p, entity)) {
                return Optional.of(p.above());
            }
        }
        return Optional.empty();
    }

    private ILog log() {
        return Mods.common(Strange.ID).log();
    }

    public class BattleTarget implements Requirement {
        static final String ENTITY_TAG = "entity";
        static final String EFFECTS_TAG = "effects";
        static final String KILLED_TAG = "killed";
        static final String UID_TAG = "uid";

        public EntityType<?> entity;
        public List<MobEffectInstance> effects = new ArrayList<>();
        public boolean killed;
        public String uid;

        private BattleTarget() {}

        public BattleTarget(EntityType<?> entity, List<MobEffectInstance> effects) {
            this.uid = makeId();
            this.entity = entity;
            this.effects = effects;
            this.killed = false;
        }

        @Override
        public boolean satisfied() {
            if (player == null) {
                return false;
            }

            return remaining() == 0;
        }

        @Override
        public int total() {
            return 1;
        }

        @Override
        public int remaining() {
            return killed ? 0 : total();
        }

        @Override
        public void load(CompoundTag tag) {
            var entityId = ResourceLocation.tryParse(tag.getString(ENTITY_TAG));
            entity = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            uid = tag.getString(UID_TAG);
            killed = tag.getBoolean(KILLED_TAG);

            effects.clear();
            var list = tag.getList(EFFECTS_TAG, 10);
            for (var t : list) {
                var effect = MobEffectInstance.load((CompoundTag)t);
                effects.add(effect);
            }
        }

        @Override
        public void save(CompoundTag tag) {
            var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
            tag.putString(ENTITY_TAG, entityId.toString());
            tag.putString(UID_TAG, uid);
            tag.putBoolean(KILLED_TAG, killed);

            var list = new ListTag();
            for (var effect : effects) {
                var effectTag = new CompoundTag();
                effect.save(effectTag);
                list.add(effectTag);
            }

            tag.put(EFFECTS_TAG, list);
        }

        @Override
        public void entityKilled(LivingEntity entity, DamageSource source) {
            checkKilled(entity);
        }

        @Override
        public void entityLeave(Entity entity) {
            checkKilled(entity);
        }

        @SuppressWarnings("unchecked")
        public boolean trySpawn() {
            if (player == null || !(player instanceof ServerPlayer serverPlayer)) {
                return false;
            }

            var pos = player.blockPosition();
            var level = (ServerLevel)serverPlayer.level();
            var questId = id();

            var found = findRandomSpawnPos(entity, level, pos);
            if (found.isPresent()) {
                MobHelper.spawn((EntityType<? extends Mob>) entity, level, found.get(), MobSpawnType.EVENT, mob -> {
                    mob.addTag(QUEST_PREFIX + questId);
                    mob.addTag(UID_PREFIX + uid);
                    mob.setTarget(player);
                    mob.setAggressive(true);
                    effects.forEach(mob::addEffect);
                });
            } else {
                log().debug(getClass(), "Could not find valid spawn pos for target " + entity);
                return false;
            }

            return true;
        }

        private void checkKilled(Entity entity) {
            if (entity.getType() == this.entity) {
                var tags = entity.getTags();
                if (tags.contains(UID_PREFIX + uid)) {
                    this.killed = true;
                    setDirty(true);
                }
            }
        }
    }
}
