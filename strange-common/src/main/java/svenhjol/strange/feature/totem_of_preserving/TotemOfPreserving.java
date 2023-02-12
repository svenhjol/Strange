package svenhjol.strange.feature.totem_of_preserving;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.Charm;
import svenhjol.charm_api.event.PlayerInventoryDropEvent;
import svenhjol.charm_api.iface.IClearsTotemInventories;
import svenhjol.charm_api.iface.IHasTotemInventories;
import svenhjol.charm_core.annotation.Configurable;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.helper.TextHelper;
import svenhjol.charm_core.init.AdvancementHandler;
import svenhjol.charm_core.init.CharmApi;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Feature(mod = Charm.MOD_ID, description = "The player's inventory items will be preserved in the Totem of Preserving upon death.")
public class TotemOfPreserving extends CharmFeature implements IHasTotemInventories, IClearsTotemInventories {
    private static final String ID = "totem_of_preserving";
    private static final String HOLDER_ID = "totem_of_preserving_holder";
    private static final ResourceLocation ADVANCEMENT = Charm.makeId("used_totem_of_preserving");
    public static Supplier<TotemItem> ITEM;
    public static Supplier<TotemBlock> BLOCK;
    public static Supplier<TotemBlock.BlockItem> BLOCK_ITEM;
    public static Supplier<BlockEntityType<TotemBlockEntity>> BLOCK_ENTITY;

    @Configurable(name = "Owner only", description = "If true, only the owner of the totem may retrieve items.")
    public static boolean ownerOnly = false;

    @Configurable(name = "Show death position", description = "If true, the coordinates where player died will be displayed in the chat screen.")
    public static boolean showDeathPosition = false;

    @Override
    public void register() {
        ITEM = Strange.REGISTRY.item(ID, () -> new TotemItem(this));
        BLOCK = Strange.REGISTRY.block(HOLDER_ID, () -> new TotemBlock(this));
        BLOCK_ITEM = Strange.REGISTRY.item(HOLDER_ID, () -> new TotemBlock.BlockItem(this, BLOCK));
        BLOCK_ENTITY = Strange.REGISTRY.blockEntity(ID, () -> TotemBlockEntity::new, List.of(BLOCK));

        CharmApi.registerProvider(this);
    }

    @Override
    public List<Function<Player, List<ItemStack>>> getTotemInventories() {
        return List.of(
            player -> player.getInventory().items,
            player -> player.getInventory().armor,
            player -> player.getInventory().offhand
        );
    }

    @Override
    public List<Consumer<Player>> clearTotemInventories() {
        return List.of(
            player -> {
                var inventories = List.of(
                    player.getInventory().items,
                    player.getInventory().armor,
                    player.getInventory().offhand
                );
                for (var inv : inventories) {
                    Collections.fill(inv, ItemStack.EMPTY);
                }
            }
        );
    }

    @Override
    public void runWhenEnabled() {
        PlayerInventoryDropEvent.INSTANCE.handle(this::handlePlayerInventoryDrop);
    }

    private InteractionResult handlePlayerInventoryDrop(Player player, Inventory inventory) {
        if (player.level.isClientSide()) {
            return InteractionResult.PASS;
        }

        var serverPlayer = (ServerPlayer) player;

        // Add all inventories.
        List<ItemStack> items = new ArrayList<>();
        CharmApi.getProviderData(IHasTotemInventories.class, s -> s.getTotemInventories().stream())
            .forEach(i -> items.addAll(i.apply(serverPlayer)));

        var preserve = items.stream()
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());

        // Don't spawn if there are no items to add.
        if (items.isEmpty()) {
            Charm.LOG.debug(getClass(), "No items found to store in totem, giving up.");
            return InteractionResult.PASS;
        }

        // Try spawn a totem.
        var result = spawnTotem(preserve, serverPlayer);
        if (!result) {
            return InteractionResult.PASS;
        }

        // Clear all inventories.
        CharmApi.getProviderData(IClearsTotemInventories.class, s -> s.clearTotemInventories().stream())
            .forEach(i -> i.accept(serverPlayer));

        return InteractionResult.SUCCESS;
    }

    private boolean spawnTotem(List<ItemStack> items, ServerPlayer player) {
        var level = player.getLevel();
        var random = player.getRandom();
        var uuid = player.getUUID();
        var message = player.getScoreboardName();

        // Get the death position.
        var pos = player.blockPosition();
        var vehicle = player.getVehicle();
        if (vehicle != null) {
            pos = vehicle.blockPosition();
        }

        // Do spawn checks.
        BlockPos spawnPos = null;
        var maxHeight = level.getMaxBuildHeight() - 1;
        var minHeight = level.getMinBuildHeight() + 1;
        var state = level.getBlockState(pos);
        var fluid = level.getFluidState(pos);

        // Adjust for void.
        if (pos.getY() < minHeight) {
            Charm.LOG.debug(getClass(), "(Void check) Adjusting, new pos: " + pos);
            pos = new BlockPos(pos.getX(), level.getSeaLevel(), pos.getZ());
        }

        if (state.isAir() || fluid.is(FluidTags.WATER)) {

            // Air and water are valid spawn positions.
            Charm.LOG.debug(getClass(), "(Standard check) Found an air/water block to spawn in: " + pos);
            spawnPos = pos;

        } else if (fluid.is(FluidTags.LAVA)) {

            // Lava: Keep moving upward to find air pocket, give up if solid (or void) reached.
            for (int tries = 0; tries < 20; tries++) {
                var y = pos.getY() + tries;
                if (y >= maxHeight) break;

                var tryPos = new BlockPos(pos.getX(), y, pos.getZ());
                var tryState = level.getBlockState(tryPos);
                var tryFluid = level.getFluidState(tryPos);
                if (tryFluid.is(FluidTags.LAVA)) continue;

                if (tryState.isAir() || tryFluid.is(FluidTags.WATER)) {
                    Charm.LOG.debug(getClass(), "(Lava check) Found an air/water block to spawn in after checking " + tries + " times: " + pos);
                    spawnPos = tryPos;
                }

                break;
            }

            // If that failed, replace the lava with the totem.
            if (spawnPos == null) {
                Charm.LOG.debug(getClass(), "(Lava check) Going to replace lava with totem at: " + pos);
                spawnPos = pos;
            }

        } else {

            // Solid block: Check above and nesw for an air or water pocket.
            List<Direction> directions = Arrays.asList(
                Direction.UP, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH
            );
            for (var direction : directions) {
                var tryPos = pos.relative(direction);
                var tryState = level.getBlockState(tryPos);
                var tryFluid = level.getFluidState(tryPos);

                if (tryPos.getY() >= maxHeight) continue;
                if (tryState.isAir() || tryFluid.is(FluidTags.WATER)) {
                    Charm.LOG.debug(getClass(), "(Solid check) Found an air/water block to spawn in, direction: " + direction + ", pos: " + pos);
                    spawnPos = tryPos;
                    break;
                }
            }
        }

        if (spawnPos == null) {

            // Try and find a valid pos within 8 blocks of the death position.
            for (var tries = 0; tries < 8; tries++) {
                var x = pos.getX() + random.nextInt(tries + 1) - tries;
                var z = pos.getZ() + random.nextInt(tries + 1) - tries;

                // If upper void reached, count downward.
                var y = pos.getY() + tries;
                if (y > maxHeight) {
                    y = pos.getY() - tries;
                    if (y < minHeight) continue;
                }

                // Look for fluid and replacable solid blocks at increasing distance from the death point.
                var tryPos = new BlockPos(x, y, z);
                var tryState = level.getBlockState(tryPos);
                var tryFluid = level.getFluidState(tryPos);
                if (tryState.isAir() || tryFluid.is(FluidTags.WATER)) {
                    Charm.LOG.debug(getClass(), "(Distance check) Found an air/water block to spawn in after checking " + tries + " times: " + pos);
                    spawnPos = tryPos;
                    break;
                }
            }

        }

        if (spawnPos == null) {
            Charm.LOG.debug(getClass(), "Could not find a block to spawn totem, giving up.");
            return false;
        }

        level.setBlockAndUpdate(spawnPos, BLOCK.get().defaultBlockState());
        if (!(level.getBlockEntity(spawnPos) instanceof TotemBlockEntity totem)) {
            Charm.LOG.debug(getClass(), "Not a valid block entity at pos, giving up. Pos: " + pos);
            return false;
        }

        totem.setItems(items);
        totem.setMessage(message);
        totem.setOwner(uuid);
        totem.setChanged();

        ProtectedPositions.add(level.dimension().location(), spawnPos);
        AdvancementHandler.trigger(ADVANCEMENT, player);

        Charm.LOG.info(getClass(), "Spawned a totem at: " + spawnPos);

        // Show the death position as chat message.
        if (showDeathPosition) {
            var x = spawnPos.getX();
            var y = spawnPos.getY();
            var z = spawnPos.getZ();

            player.displayClientMessage(TextHelper.translatable("gui.charm.totem_of_preserving.deathpos", x, y, z), false);
        }

        return true;
    }
}
