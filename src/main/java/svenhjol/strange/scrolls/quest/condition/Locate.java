package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.WorldHelper;
import svenhjol.strange.base.helper.QuestHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.Quests;
import svenhjol.strange.scrolls.module.Scrolls;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Locate implements IDelegate
{
    public static final String ID = "Locate";

    private final String STACK = "stack";
    private final String LOCATION = "location";
    private final String DIM = "dim";
    private final String LOCATED = "located";
    private final String SPAWNED = "spawned";

    private IQuest quest;
    private BlockPos location;
    private ItemStack stack;
    private int dim;
    private boolean located;
    private boolean spawned;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (event instanceof QuestEvent.Accept) {
            final QuestEvent.Accept qe = (QuestEvent.Accept) event;
            return onStarted(qe.getQuest(), player);
        }

        if (isSatisfied()) return false;
        if (player == null) return false;

        if (event instanceof QuestEvent.Complete || event instanceof QuestEvent.Fail || event instanceof QuestEvent.Decline) {
            return onEnded(player);
        }

        if (event instanceof PlayerTickEvent) {
            return onTick(player);
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return this.located;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        return this.located ? 100 : 0;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putLong(LOCATION, location != null ? location.toLong() : 0);
        tag.putInt(DIM, dim);
        tag.putBoolean(SPAWNED, spawned);
        tag.putBoolean(LOCATED, located);

        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.location = BlockPos.fromLong(data.getLong(LOCATION));
        this.dim = data.getInt(DIM);
        this.spawned = data.getBoolean(SPAWNED);
        this.located = data.getBoolean(LOCATED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public boolean shouldRemove()
    {
        return false;
    }

    public boolean onStarted(IQuest quest, PlayerEntity player)
    {
        if (quest.getId().equals(this.quest.getId())) {
            Random rand = player.world.rand;
            int dist = Scrolls.locateDistance;

            int x = -(dist/2) + rand.nextInt(dist);
            int z = -(dist/2) + rand.nextInt(dist);
            this.location = player.getPosition().add(x, 0, z);
            this.dim = WorldHelper.getDimensionId(player.world);

            QuestHelper.giveLocationItemToPlayer(player, quest, this.location, this.dim);
            return true;
        }

        return false;
    }

    public boolean onTick(PlayerEntity player)
    {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        boolean atLocation = WorldHelper.getDistanceSq(playerPos, this.location) < 8;

        if (this.spawned && !this.located && world.getGameTime() % 20 == 0) {

            for (ItemStack s : player.inventory.mainInventory) {
                CompoundNBT tag = s.getTag();
                if (tag == null || tag.isEmpty()) continue;

                String key = Locate.ID + "_" + Quests.QUEST_ID;

                String id = tag.getString(key);
                if (id.isEmpty() || !id.equals(quest.getId())) continue;

                this.located = true;
                tag.remove(key);
                s.shrink(s.getCount());
                break;
            }

            if (this.located) {
                QuestHelper.effectCompleted(player, new TranslationTextComponent("event.strange.quests.located_all"));
            }
            return true;
        }

        if (!this.spawned && atLocation) {
            // create chest at location
            Random rand = world.rand;
            int range = 4;
            int tries = 4;
            int start = 16 + rand.nextInt(16);
            List<BlockPos> validPositions = new ArrayList<>();

            for(int y = start; y < world.getSeaLevel(); y++) {
                for(int i = 2; i < range; i++) {
                    for(int c = 1; c < tries; ++c) {
                        BlockPos p = new BlockPos(playerPos.getX() - (range / 2) + rand.nextInt(range), y, playerPos.getZ() - (range / 2) + rand.nextInt(range));
                        BlockState floor = world.getBlockState(p.down());
                        boolean valid = (world.isAirBlock(p) || world.getBlockState(p).getMaterial() == Material.WATER);
                        if (floor.isSolid() && valid) validPositions.add(p);
                    }
                }
            }

            BlockPos spawnPos;
            if (validPositions.isEmpty()) {
                int x = playerPos.getX() - (range / 2) + rand.nextInt(range);
                int y = 16 + rand.nextInt(16);
                int z = playerPos.getZ() - (range / 2) + rand.nextInt(range);
                spawnPos = new BlockPos(x, y, z);
                if (!world.getBlockState(spawnPos.down()).isSolid()) {
                    world.setBlockState(spawnPos.down(), Blocks.STONE.getDefaultState(), 2);
                }
            } else  {
                spawnPos = validPositions.get(rand.nextInt(validPositions.size()));
            }

            BlockState chestState = Blocks.CHEST.getDefaultState();

            // waterlogged chest
            if (world.getBlockState(spawnPos).getMaterial() == Material.WATER) {
                chestState = chestState.with(ChestBlock.WATERLOGGED, true);
            }

            world.setBlockState(spawnPos, chestState, 2);
            TileEntity tile = world.getTileEntity(spawnPos);

            if (tile instanceof ChestTileEntity) {
                // create normal dungeon loot
                ChestTileEntity chest = (ChestTileEntity) tile;
                chest.setLootTable(LootTables.CHESTS_SIMPLE_DUNGEON, rand.nextLong());

                // add the quest item to the dungeon loot
                ItemStack lootStack = stack.copy();

                CompoundNBT tag = lootStack.getOrCreateTag();
                tag.putString(Locate.ID + "_" + Quests.QUEST_ID, quest.getId());
                lootStack.setTag(tag);
                chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), lootStack);

                Meson.debug("[Locate] Spawned chest at " + spawnPos);
                this.spawned = true;
            }

            if (!this.spawned) {
                this.fail(player);
                return false;
            }

            return true;
        }

        return false;
    }

    public boolean onEnded(PlayerEntity player)
    {
        QuestHelper.removeQuestItemsFromPlayer(player, this.quest);
        return true;
    }

    public void fail(PlayerEntity player)
    {
        MinecraftForge.EVENT_BUS.post(new QuestEvent.Fail(player, quest));
    }

    public Locate setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public ItemStack getStack()
    {
        return this.stack;
    }
}
