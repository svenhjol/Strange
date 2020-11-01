package svenhjol.strange.module;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.ItemHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.PlayerDropInventoryCallback;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.TotemOfPreservingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        double y = playerPos.getY() + 2.25D;
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
        Charm.LOG.info("Totem of Preserving spawned at " + new BlockPos(x, y, z));

        // clear player's inventory
        for (DefaultedList<ItemStack> inv : combined) {
            for (int i = 0; i < inv.size(); i++) {
                inv.set(i, ItemStack.EMPTY);
            }
        }

        return ActionResult.SUCCESS;
    }
}
