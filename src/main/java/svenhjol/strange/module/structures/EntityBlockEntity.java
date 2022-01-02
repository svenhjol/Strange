package svenhjol.strange.module.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import svenhjol.charm.block.CharmSyncedBlockEntity;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

import java.util.*;

public class EntityBlockEntity extends CharmSyncedBlockEntity {
    public static final String ENTITY_TAG = "entity";
    public static final String PERSISTENT_TAG = "persist";
    public static final String HEALTH_TAG = "health";
    public static final String ARMOR_TAG = "armor";
    public static final String EFFECTS_TAG = "effects";
    public static final String META_TAG = "meta";
    public static final String COUNT_TAG = "count";
    public static final String ROTATION_TAG = "rotation";
    public static final String PRIMED_TAG = "primed";

    private String entity = "";
    private Rotation rotation = Rotation.NONE;
    private boolean persistent = true;
    private boolean primed = false;
    private double health = 20;
    private int count = 1;
    private String effects = "";
    private String armor = "";
    private String meta = "";

    public float rotateTicks = 0F;

    public EntityBlockEntity(BlockPos pos, BlockState state) {
        super(Structures.ENTITY_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.entity = tag.getString(ENTITY_TAG);
        this.persistent = tag.getBoolean(PERSISTENT_TAG);
        this.health = tag.getDouble(HEALTH_TAG);
        this.count = tag.getInt(COUNT_TAG);
        this.effects = tag.getString(EFFECTS_TAG);
        this.armor = tag.getString(ARMOR_TAG);
        this.meta = tag.getString(META_TAG);
        this.primed = tag.getBoolean(PRIMED_TAG);

        String rot = tag.getString(ROTATION_TAG);
        this.rotation = rot.isEmpty() ? Rotation.NONE : Rotation.valueOf(rot);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString(ENTITY_TAG, entity);
        tag.putString(ROTATION_TAG, rotation.name());
        tag.putBoolean(PERSISTENT_TAG, persistent);
        tag.putBoolean(PRIMED_TAG, primed);
        tag.putDouble(HEALTH_TAG, health);
        tag.putInt(COUNT_TAG, count);
        tag.putString(EFFECTS_TAG, effects);
        tag.putString(ARMOR_TAG, armor);
        tag.putString(META_TAG, meta);
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setPrimed(boolean primed) {
        this.primed = primed;
    }

    public void setPersistent(boolean persist) {
        this.persistent = persist;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setEffects(String effects) {
        this.effects = effects;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setArmor(String armor) {
        this.armor = armor;
    }

    public boolean isPrimed() {
        return primed;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public double getHealth() {
        return health;
    }

    public int getCount() {
        return count;
    }

    public String getEffects() {
        return effects;
    }

    public String getArmor() {
        return armor;
    }

    public static <T extends EntityBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T entityBlock) {
        if (level == null || level.getGameTime() % 10 == 0 || level.getDifficulty() == Difficulty.PEACEFUL || !entityBlock.isPrimed()) return;

        // fetch all survival players nearby
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(Structures.entityTriggerDistance))
            .stream().filter(p -> !p.isSpectator()).toList();

        if (players.size() == 0) return;

        // remove the entityBlock, create the entity
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        boolean result = trySpawn(level, entityBlock.worldPosition, entityBlock);

        if (result) {
            LogHelper.debug(Strange.MOD_ID, EntityBlockEntity.class, "EntityBlock spawned entity " + entityBlock.getEntity() + " at pos: " + pos);
        } else {
            LogHelper.debug(Strange.MOD_ID, EntityBlockEntity.class, "EntityBlock failed to spawn entity " + entityBlock.getEntity() + " at pos: " + pos);
        }
    }

    public static boolean trySpawn(Level level, BlockPos pos, EntityBlockEntity spawner) {
        Entity spawned;
        if (level == null) return false;

        var spawnerEntity = spawner.entity;
        if (spawnerEntity.contains("|")) {
            var strings = Arrays.stream(spawnerEntity.split("\\|")).map(String::trim).toList();
            spawnerEntity = strings.get(level.getRandom().nextInt(strings.size()));
        }

        Optional<EntityType<?>> opt = Registry.ENTITY_TYPE.getOptional(new ResourceLocation(spawnerEntity));
        if (opt.isEmpty()) return false;

        EntityType<?> type = opt.get();

        if (type == EntityType.MINECART || type == EntityType.CHEST_MINECART) {
            return tryCreateMinecart(level, pos, type, spawner);
        }

        if (type == EntityType.ARMOR_STAND) {
            return tryCreateArmorStand(level, pos, spawner);
        }

        for (int i = 0; i < spawner.count; i++) {
            spawned = type.create(level);
            if (spawned == null) return false;
            spawned.moveTo(pos, 0.0F, 0.0F);

            if (spawned instanceof Mob mob) {
                if (spawner.persistent) mob.setPersistenceRequired();

                // set the mob health if specified (values greater than zero)
                if (spawner.health > 0) {
                    // need to override this attribute on the entity to allow health values greater than maxhealth
                    AttributeInstance healthAttribute = mob.getAttribute(Attributes.MAX_HEALTH);
                    if (healthAttribute != null) {
                        healthAttribute.setBaseValue(spawner.health);
                    }

                    mob.setHealth((float) spawner.health);
                }

                // add armor to the mob
                if (!spawner.armor.isEmpty()) {
                    Random random = level.random;
                    tryEquip(mob, spawner.armor, random);
                }

                // apply status effects to the mob
                final List<String> effectsList = new ArrayList<>();
                if (spawner.effects.length() > 0) {
                    if (spawner.effects.contains(",")) {
                        effectsList.addAll(Arrays.asList(spawner.effects.split(",")));
                    } else {
                        effectsList.add(spawner.effects);
                    }
                    if (effectsList.size() > 0) {
                        effectsList.forEach(effectName -> {
                            MobEffect effect = Registry.MOB_EFFECT.get(new ResourceLocation(effectName));
                            if (effect != null) {
                                mob.addEffect(new MobEffectInstance(effect, 999999, 1)); // TODO: duration lol
                            }
                        });
                    }
                }

                mob.finalizeSpawn((ServerLevelAccessor)level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null, null);
            }

            level.addFreshEntity(spawned);
        }
        return true;
    }

    public static boolean tryCreateMinecart(Level level, BlockPos pos, EntityType<?> type, EntityBlockEntity spawner) {
        AbstractMinecart minecart = null;
        if (level == null) return false;

        if (type == EntityType.CHEST_MINECART) {
            minecart = new MinecartChest(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            String loot = Processors.getValue(spawner.meta, "loot", "");
            ResourceLocation lootTable;

            if (loot.isEmpty()) {
                lootTable = BuiltInLootTables.ABANDONED_MINESHAFT;
            } else {
                lootTable = new ResourceLocation(loot);
            }

            ((MinecartChest)minecart).setLootTable(lootTable, level.random.nextLong());
        } else if (type == EntityType.MINECART) {
            minecart = new Minecart(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        }

        if (minecart == null) return false;
        level.addFreshEntity(minecart);
        return true;
    }

    public static boolean tryCreateArmorStand(Level level, BlockPos pos, EntityBlockEntity spawner) {
        if (level == null) return false;

        Random random = level.random;
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) return false;

        Direction face = Direction.byName(Processors.getValue(spawner.meta, "facing", "north"));
        if (face == null) face = Direction.NORTH;

        Direction facing = spawner.rotation.rotate(face);
        String type = Processors.getValue(spawner.meta, "type", "");

        tryEquip(stand, type, random);
        float yaw = facing.get2DDataValue();
        stand.moveTo(pos, yaw, 0.0F);
        level.addFreshEntity(stand);

        return true;
    }

    private static void tryEquip(LivingEntity entity, String type, Random random) {
        List<Item> ironHeld = new ArrayList<>(Arrays.asList(
            Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE
        ));

        List<Item> goldHeld = new ArrayList<>(Arrays.asList(
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE
        ));

        List<Item> diamondHeld = new ArrayList<>(Arrays.asList(
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL
        ));

        if (type.equals("leather")) {
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ironHeld.get(random.nextInt(ironHeld.size()))));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        }
        if (type.equals("chain")) {
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ironHeld.get(random.nextInt(ironHeld.size()))));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        if (type.equals("iron")) {
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ironHeld.get(random.nextInt(ironHeld.size()))));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        }
        if (type.equals("gold")) {
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(goldHeld.get(random.nextInt(goldHeld.size()))));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }
        if (type.equals("diamond")) {
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(diamondHeld.get(random.nextInt(diamondHeld.size()))));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            if (random.nextFloat() < 0.25F)
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        }
    }
}
