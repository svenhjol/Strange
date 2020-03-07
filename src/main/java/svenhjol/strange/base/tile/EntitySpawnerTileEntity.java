package svenhjol.strange.base.tile;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.DecorationHelper;
import svenhjol.strange.base.module.EntitySpawner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EntitySpawnerTileEntity extends TileEntity implements ITickableTileEntity {
    private final static String ENTITY = "entity";
    private final static String PERSIST = "persist";
    private final static String HEALTH = "health";
    private final static String META = "meta";
    private final static String COUNT = "count";
    private final static String ROTATION = "rotation";

    public ResourceLocation entity = null;
    public Rotation rotation = Rotation.NONE;
    public boolean persist = false;
    public double health = 0;
    public int count = 1;
    public String meta = "";

    public EntitySpawnerTileEntity() {
        super(EntitySpawner.tile);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        this.entity = ResourceLocation.tryCreate(tag.getString(ENTITY));
        this.persist = tag.getBoolean(PERSIST);
        this.health = tag.getDouble(HEALTH);
        this.count = tag.getInt(COUNT);
        this.meta = tag.getString(META);
        String rot = tag.getString(ROTATION);
        this.rotation = rot.isEmpty() ? Rotation.NONE : Rotation.valueOf(rot);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putString(ENTITY, entity.toString());
        tag.putString(ROTATION, rotation.name());
        tag.putBoolean(PERSIST, persist);
        tag.putDouble(HEALTH, health);
        tag.putInt(COUNT, count);
        tag.putString(META, meta);
        return tag;
    }

    @Override
    public void tick() {
        if (world == null
            || world.getGameTime() % 10 == 0
            || world.getDifficulty() == Difficulty.PEACEFUL
        ) return;

        BlockPos pos = getPos();
        List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos).grow(EntitySpawner.triggerDistance));
        if (players.size() == 0) return;

        // remove the spawner, create the entity
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        boolean result = trySpawn(pos);
        if (result) {
            Strange.LOG.debug("EntitySpawner spawned entity at pos: " + pos);
        } else {
            Strange.LOG.warn("EntitySpawner failed to spawn entity at pos: " + pos);
        }
    }

    public boolean trySpawn(BlockPos pos) {
        Entity ent;
        EntityType<?> type;

        if (world == null) return false;

        type = ForgeRegistries.ENTITIES.getValue(entity);
        if (type == null) return false;

        if (type == EntityType.MINECART || type == EntityType.CHEST_MINECART) {
            return tryCreateMinecart(type, pos);
        }

        if (type == EntityType.ARMOR_STAND) {
            return tryCreateArmorStand(pos);
        }

        for (int i = 0; i < this.count; i++) {
            ent = type.create(world);
            if (ent == null) return false;
            ent.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);

            if (ent instanceof MobEntity) {
                MobEntity m = (MobEntity) ent;
                if (persist) m.enablePersistence();
                if (health > 0) m.setHealth((float) health);
                m.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.TRIGGERED, null, null);
            }

            world.addEntity(ent);
        }
        return true;
    }

    public boolean tryCreateMinecart(EntityType<?> type, BlockPos pos) {
        AbstractMinecartEntity minecart = null;
        if (world == null) return false;

        if (type == EntityType.CHEST_MINECART) {
            minecart = new ChestMinecartEntity(world.getWorld(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            ResourceLocation lootTable = DecorationHelper.getLootTable(this.meta, LootTables.CHESTS_ABANDONED_MINESHAFT);
            ((ChestMinecartEntity) minecart).setLootTable(lootTable, world.rand.nextLong());
        } else if (type == EntityType.MINECART) {
            minecart = new MinecartEntity(world.getWorld(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        }

        if (minecart == null) return false;
        world.addEntity(minecart);
        return true;
    }

    public boolean tryCreateArmorStand(BlockPos pos) {
        if (world == null) return false;
        Random rand = world.rand;

        ArmorStandEntity stand = EntityType.ARMOR_STAND.create(world.getWorld());
        if (stand == null) return false;

        Direction face = DecorationHelper.getFacing(DecorationHelper.getValue("facing", this.meta, "north"));
        Direction facing = this.rotation.rotate(face);

        String type = DecorationHelper.getValue("type", this.meta, "");

        List<Item> ironHeld = new ArrayList<>(Arrays.asList(
            Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE
        ));
        List<Item> goldHeld = new ArrayList<>(Arrays.asList(
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE
        ));
        List<Item> diamondHeld = new ArrayList<>(Arrays.asList(
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL
        ));

        if (type.equals("chain")) {
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ironHeld.get(rand.nextInt(ironHeld.size()))));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        if (type.equals("iron")) {
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(ironHeld.get(rand.nextInt(ironHeld.size()))));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.IRON_HELMET));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
        }
        if (type.equals("gold")) {
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(goldHeld.get(rand.nextInt(goldHeld.size()))));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }
        if (type.equals("diamond")) {
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(diamondHeld.get(rand.nextInt(diamondHeld.size()))));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            if (rand.nextFloat() < 0.25F)
                stand.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        }

        float yaw = facing.getHorizontalAngle();
        stand.moveToBlockPosAndAngles(pos, yaw, 0.0F);
        world.addEntity(stand);
        return true;
    }
}
