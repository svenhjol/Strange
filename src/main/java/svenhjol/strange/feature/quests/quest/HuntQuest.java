package svenhjol.strange.feature.quests.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import svenhjol.strange.data.LinkedEntityTypeList;
import svenhjol.strange.data.ResourceListManager;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HuntQuest extends Quest {
    static final String REQUIRED_KILLS_TAG = "required";

    final List<HuntTarget> targets = new ArrayList<>();

    @Override
    public List<? extends Requirement> requirements() {
        return targets;
    }

    @Override
    public void entityKilled(LivingEntity entity, DamageSource source) {
        targets.forEach(t -> t.entityKilled(entity, source));
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        targets.clear();
        var list = tag.getList(REQUIRED_KILLS_TAG, 10);
        for (var t : list) {
            var target = new HuntTarget();
            target.load((CompoundTag)t);
            targets.add(target);
        }
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
    }

    @Override
    protected void makeRequirements(ResourceManager manager, QuestDefinition definition) {
        var entries = ResourceListManager.entries(manager, "quests/hunt");

        // Populate the mobs.
        var mobEntries = definition.pair(definition.huntMobs(), random());
        var mobEntry = mobEntries.getFirst();
        var mobAmount = mobEntries.getSecond();

        var targets = LinkedEntityTypeList.load(entries.getOrDefault(mobEntry, new LinkedList<>()));
        if (targets.isEmpty()) {
            throw new RuntimeException("Entity list is empty");
        }

        var maxSelection = Math.min(4, villagerLevel());
        Collections.shuffle(targets);
        var selection = Math.min(targets.size(), villagerLevel() + random().nextInt(maxSelection) + 1);

        for (int i = 0; i < selection; i++) {
            var entity = targets.get(i);
            this.targets.add(new HuntTarget(entity, mobAmount / selection));
        }
    }

    public class HuntTarget implements Requirement {
        static final String ENTITY_TAG = "entity";
        static final String TOTAL_TAG = "total";
        static final String KILLED_TAG = "killed";

        public EntityType<?> entity;
        public int total;
        public int killed;

        private HuntTarget() {}

        public HuntTarget(EntityType<?> entity, int total) {
            this.entity = entity;
            this.total = total;
            this.killed = 0;
        }

        @Override
        public void load(CompoundTag tag) {
            var entityId = ResourceLocation.tryParse(tag.getString(ENTITY_TAG));

            entity = entityRegistry().get(entityId);
            total = tag.getInt(TOTAL_TAG);
            killed = tag.getInt(KILLED_TAG);
        }

        @Override
        public void save(CompoundTag tag) {
            var entityId = entityRegistry().getKey(entity);
            if (entityId == null) {
                throw new RuntimeException("Could not parse entity");
            }

            tag.putString(ENTITY_TAG, entityId.toString());
            tag.putInt(TOTAL_TAG, total);
            tag.putInt(KILLED_TAG, killed);
        }

        @Override
        public void entityKilled(LivingEntity entity, DamageSource source) {
            if (entity.getType().equals(this.entity)) {
                this.killed++;
            }
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
            return total;
        }

        @Override
        public int remaining() {
            return Math.max(0, total - killed);
        }
    }
}
