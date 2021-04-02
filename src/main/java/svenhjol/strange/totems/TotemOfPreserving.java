package svenhjol.strange.totems;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.ItemHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDropsXpCallback;
import svenhjol.charm.event.PlayerDropInventoryCallback;
import svenhjol.strange.Strange;
import svenhjol.strange.traveljournals.JournalEntry;
import svenhjol.strange.traveljournals.TravelJournalManager;
import svenhjol.strange.traveljournals.TravelJournals;

import java.util.*;

@Module(mod = Strange.MOD_ID, description = "With a Totem of Preserving in your inventory, your items will be held in the totem when you die.")
public class TotemOfPreserving extends CharmModule {
    public static TotemOfPreservingItem TOTEM_OF_PRESERVING;

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
        EntityDropsXpCallback.BEFORE.register(this::tryInterceptDropXp);
    }

    public ActionResult tryInterceptDropXp(LivingEntity entity) {
        if (!preserveXp || !(entity instanceof PlayerEntity) || entity.world.isClient)
            return ActionResult.PASS;

        return ActionResult.SUCCESS;
    }

    public ActionResult tryInterceptDropInventory(PlayerEntity player, PlayerInventory inventory) {
        if (player.world.isClient)
            return ActionResult.PASS;

        ServerWorld world = (ServerWorld)player.world;
        Random random = world.getRandom();
        ItemStack totem = new ItemStack(TOTEM_OF_PRESERVING);
        NbtCompound serialized = new NbtCompound();
        List<ItemStack> holdable = new ArrayList<>();
        List<DefaultedList<ItemStack>> combined = ImmutableList.of(inventory.main, inventory.armor, inventory.offHand);

        combined.forEach(list
            -> list.stream().filter(Objects::nonNull).filter(stack -> !stack.isEmpty()).forEach(holdable::add));

        List<ItemStack> totemsToSpawn = new ArrayList<>();

        // get all inventories and store them in the totem
        for (int i = 0; i < holdable.size(); i++) {
            ItemStack stack = holdable.get(i);

            // if there's already a filled totem in the inventory, spawn this separately
            if (stack.getItem() == TOTEM_OF_PRESERVING && !TotemOfPreservingItem.getItems(stack).isEmpty()) {
                totemsToSpawn.add(stack);
                continue;
            }

            serialized.put(Integer.toString(i), holdable.get(i).writeNbt(new NbtCompound()));
        }

        TotemOfPreservingItem.setItems(totem, serialized);
        TotemOfPreservingItem.setMessage(totem, player.getEntityName());

        if (preserveXp)
            TotemOfPreservingItem.setXp(totem, player.totalExperience);

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
}
