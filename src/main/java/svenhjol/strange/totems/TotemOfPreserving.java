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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.ItemHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDropsCallback;
import svenhjol.charm.event.PlayerDropInventoryCallback;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.item.Items.TOTEM_OF_UNDYING;

@Module(mod = Strange.MOD_ID, description = "With a Totem of Preserving in your inventory, your items will be held in the totem when you die.")
public class TotemOfPreserving extends CharmModule {
    public static TotemOfPreservingItem TOTEM_OF_PRESERVING;

    @Config(name = "Grave Mode", description = "If true, your items will always drop as a totem even if you don't have one in your inventory.")
    public static boolean graveMode = false;

    @Override
    public void register() {
        TOTEM_OF_PRESERVING = new TotemOfPreservingItem(this);
    }

    @Override
    public void init() {
        ItemHelper.ITEM_LIFETIME.put(TOTEM_OF_PRESERVING, Integer.MAX_VALUE); // probably stupid
        PlayerDropInventoryCallback.EVENT.register(this::tryInterceptDropInventory);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        EntityDropsCallback.EVENT.register(this::tryDropTotemFromIllusioner);
    }

    public ActionResult tryInterceptDropInventory(PlayerEntity player, PlayerInventory inventory) {
        if (player.world.isClient)
            return ActionResult.PASS;

        ServerWorld world = (ServerWorld)player.world;
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

        // get all inventories and store them in the totem
        for (int i = 0; i < holdable.size(); i++) {
            serialized.put(Integer.toString(i), holdable.get(i).toTag(new CompoundTag()));
        }

        TotemOfPreservingItem.setItems(totem, serialized);
        TotemOfPreservingItem.setMessage(totem, player.getEntityName());

        BlockPos playerPos = player.getBlockPos();
        double x = playerPos.getX() + 0.5D;
        double y = playerPos.getY() + 1.0D;
        double z = playerPos.getZ() + 0.5D;

        if (y < 1)
            y = 64; // fetching your totem from the void is sad

        ItemEntity totemEntity = new ItemEntity(world, x, y, z, totem);
        totemEntity.setNoGravity(true);
        totemEntity.setVelocity(0, 0, 0);
        totemEntity.setPos(x, y, z);
        totemEntity.setCovetedItem();
        totemEntity.setGlowing(true);
        totemEntity.setInvulnerable(true);

        world.spawnEntity(totemEntity);
        Criteria.USED_TOTEM.trigger((ServerPlayerEntity)player, totem);
        Charm.LOG.info("Totem of Preserving spawned at " + new BlockPos(x, y, z));

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
