package svenhjol.strange.feature.quests.quest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.feature.quests.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HuntQuest extends Quest<EntityType<?>> {
    static final int MAX_SELECTION = 3;
    static final String REQUIRED_KILLS_TAG = "required";

    final List<HuntTarget> targets = new ArrayList<>();

    @Override
    protected Registry<EntityType<?>> registry() {
        return BuiltInRegistries.ENTITY_TYPE;
    }

    @Override
    protected ResourceKey<Registry<EntityType<?>>> resourceKey() {
        return Registries.ENTITY_TYPE;
    }

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
        for (Tag t : list) {
            var item = new HuntTarget();
            item.load((CompoundTag)t);
            targets.add(item);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        var list = new ListTag();
        for (HuntTarget item : targets) {
            var t = new CompoundTag();
            item.save(t);
            list.add(t);
        }
        tag.put(REQUIRED_KILLS_TAG, list);
    }

    @Override
    protected void make(QuestDefinition definition, UUID villagerUuid) {
        this.id = makeId();
        this.type = definition.type();
        this.status = Status.NOT_STARTED;
        this.epic = definition.isEpic();
        this.villagerProfession = definition.profession();
        this.villagerLevel = definition.level();
        this.villagerUuid = villagerUuid;

        var random = RandomSource.create();
        makeRequirements(definition, random);
        makeRewards(definition, random);
    }

    protected void makeRequirements(QuestDefinition definition, RandomSource random) {
        var requirement = definition.randomRequirement(random);
        var requiredEntity = requirement.getFirst();
        var requiredTotal = requirement.getSecond();
        List<ResourceLocation> values = new ArrayList<>();

        for (EntityType<?> entity : TagHelper.getValues(registry(), tag(requiredEntity))) {
            values.add(registry().getKey(entity));
        }

        Collections.shuffle(values);
        var selection = Math.min(values.size(), random.nextInt(MAX_SELECTION) + 1);

        for (int i = 0; i < selection; i++) {
            var entity = registry().get(values.get(i));
            targets.add(new HuntTarget(entity, requiredTotal / selection));
        }
    }

    public class HuntTarget implements Requirement {
        static final String ENTITY_TAG = "entity";
        static final String TOTAL_TAG = "total";
        static final String KILLED_TAG = "killed";

        public EntityType<?> entity;
        public int total;
        public int killed;

        public HuntTarget() {}

        public HuntTarget(EntityType<?> entity, int total) {
            this.entity = entity;
            this.total = total;
            this.killed = 0;
        }

        @Override
        public void load(CompoundTag tag) {
            var entityId = ResourceLocation.tryParse(tag.getString(ENTITY_TAG));

            entity = registry().get(entityId);
            total = tag.getInt(TOTAL_TAG);
            killed = tag.getInt(KILLED_TAG);
        }

        @Override
        public void save(CompoundTag tag) {
            var entityId = registry().getKey(entity);
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
            if (player == null) {
                return total;
            }

            return Math.max(0, total - killed);
        }
    }
}
