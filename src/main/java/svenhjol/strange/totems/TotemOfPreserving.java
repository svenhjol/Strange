package svenhjol.strange.totems;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.ItemHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDropsCallback;
import svenhjol.charm.event.PlayerDropInventoryCallback;
import svenhjol.strange.Strange;
import svenhjol.strange.traveljournals.JournalEntry;
import svenhjol.strange.traveljournals.TravelJournalManager;
import svenhjol.strange.traveljournals.TravelJournals;

import java.util.*;

import static net.minecraft.item.Items.TOTEM_OF_UNDYING;

@Module(mod = Strange.MOD_ID, description = "With a Totem of Preserving in your inventory, your items will be held in the totem when you die.")
public class TotemOfPreserving extends CharmModule {
    public static TotemOfPreservingItem TOTEM_OF_PRESERVING;

    @Config(name = "Grave Mode", description = "If true, your items will always drop as a totem even if you don't have one in your inventory.")
    public static boolean graveMode = true;

    @Config(name = "Preserve XP", description = "If true, the totem will preserve the player's experience and restore when broken.")
    public static boolean preserveXp = false;

    @Override
    public void register() {
        TOTEM_OF_PRESERVING = new TotemOfPreservingItem(this);
    }

    @Override
    public void init() {
        ItemHelper.ITEM_LIFETIME.put(TOTEM_OF_PRESERVING, Integer.MAX_VALUE); // probably stupid
        PlayerDropInventoryCallback.EVENT.register(this::tryInterceptDropInventory);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        EntityDropsCallback.AFTER.register(this::tryDropTotemFromIllusioner);
    }

    public ActionResult tryInterceptDropInventory(PlayerEntity player, PlayerInventory inventory) {
        if (player.world.isClient)
            return ActionResult.PASS;

        ServerWorld world = (ServerWorld)player.world;
        Random random = world.getRandom();
        ItemStack totem = new ItemStack(TOTEM_OF_PRESERVING);
        CompoundTag serialized = new CompoundTag();
        List<ItemStack> holdable = new ArrayList<>();
        List<DefaultedList<ItemStack>> combined = ImmutableList.of(inventory.main, inventory.armor, inventory.offHand);

        combined.forEach(list
            -> list.stream().filter(Objects::nonNull).filter(stack -> !stack.isEmpty()).forEach(holdable::add));

        // check all inventories for totem
        if (!graveMode) {
            boolean found = false;
            int foundAtIndex = 0;

            for (int i = 0; i < holdable.size(); i++) {
                ItemStack stack = holdable.get(i);
                if (stack.getItem() == TOTEM_OF_PRESERVING && TotemOfPreservingItem.getItems(stack).isEmpty()) {
                    foundAtIndex = i;
                    found = true;
                }
            }

            if (found) {
                holdable.remove(foundAtIndex);
            } else {
                return ActionResult.FAIL;
            }
        }

        List<ItemStack> totemsToSpawn = new ArrayList<>();

        // get all inventories and store them in the totem
        for (int i = 0; i < holdable.size(); i++) {
            ItemStack stack = holdable.get(i);

            // if there's already a filled totem in the inventory, spawn this separately
            if (stack.getItem() == TOTEM_OF_PRESERVING && !TotemOfPreservingItem.getItems(stack).isEmpty()) {
                totemsToSpawn.add(stack);
                continue;
            }

            serialized.put(Integer.toString(i), holdable.get(i).toTag(new CompoundTag()));
        }

        TotemOfPreservingItem.setItems(totem, serialized);
        TotemOfPreservingItem.setMessage(totem, player.getEntityName());

        if (!TotemOfPreservingItem.getItems(totem).isEmpty())
            totemsToSpawn.add(totem);

        BlockPos playerPos = player.getBlockPos();

        double x = playerPos.getX() + 0.25D;
        double y = playerPos.getY() + 0.75D;
        double z = playerPos.getZ() + 0.25D;

        if (y < 1)
            y = 64; // fetching your totem from the void is sad

        // spawn totems
        for (ItemStack stack : totemsToSpawn) {
            double tx = x + random.nextFloat() * 0.25D;
            double ty = y + random.nextFloat() * 0.25D;
            double tz = z + random.nextFloat() * 0.25D;

            ItemEntity totemEntity = new ItemEntity(world, x, y, z, stack);
            totemEntity.setNoGravity(true);
            totemEntity.setVelocity(0, 0, 0);
            totemEntity.setPos(tx, ty, tz);
            totemEntity.setCovetedItem();
            totemEntity.setGlowing(true);
            totemEntity.setInvulnerable(true);

            world.spawnEntity(totemEntity);
        }

        Criteria.USED_TOTEM.trigger((ServerPlayerEntity)player, totem);
        Charm.LOG.info("Totem of Preserving spawned at " + new BlockPos(x, y, z));

        // add position to travel journal
        Optional<TravelJournalManager> journalManager = TravelJournals.getTravelJournalManager();
        journalManager.ifPresent(manager -> {
            JournalEntry entry = new JournalEntry(
                new TranslatableText("item.strange.totem_of_preserving").getString(),
                playerPos,
                DimensionHelper.getDimension(world),
                1
            );
            manager.addJournalEntry(player, entry);
        });

        // clear player's inventory
        for (DefaultedList<ItemStack> inv : combined) {
            for (int i = 0; i < inv.size(); i++) {
                inv.set(i, ItemStack.EMPTY);
            }
        }

        return ActionResult.SUCCESS;
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (graveMode)
            return;

        if (id.equals(LootTables.PILLAGER_OUTPOST_CHEST)
            || id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(UniformLootTableRange.between(0.0F, 1.0F))
                .with(ItemEntry.builder(TOTEM_OF_PRESERVING));

            supplier.pool(builder);
        }
    }

    public ActionResult tryDropTotemFromIllusioner(LivingEntity entity, DamageSource source, int lootingLevel) {
        if (!entity.world.isClient
            && entity instanceof IllusionerEntity
        ) {
            World world = entity.getEntityWorld();
            BlockPos pos = entity.getBlockPos();

            List<Item> totems = new ArrayList<>();
            totems.add(TOTEM_OF_UNDYING);
            totems.add(TOTEM_OF_PRESERVING);

            if (ModuleHandler.enabled(TotemOfWandering.class))
                totems.add(TotemOfWandering.TOTEM_OF_WANDERING);

            ItemStack totem = new ItemStack(totems.get(world.random.nextInt(totems.size())));
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), totem));
        }

        return ActionResult.PASS;
    }
}
