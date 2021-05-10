package svenhjol.strange.storagecrates;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.enums.IVariantMaterial;
import svenhjol.charm.base.enums.VanillaVariantMaterial;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.helper.RegistryHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, priority = 10, client = StorageCratesClient.class)
public class StorageCrates extends CharmModule {
    public static final Identifier ID = new Identifier(Strange.MOD_ID, "storage_crate");
    public static final Identifier MSG_CLIENT_INTERACTED_WITH_CRATE = new Identifier(Strange.MOD_ID, "client_interacted_with_crate");
    public static Map<IVariantMaterial, StorageCrateBlock> STORAGE_CRATE_BLOCKS = new HashMap<>();
    public static BlockEntityType<StorageCrateBlockEntity> BLOCK_ENTITY;

    @Config(name = "Maximum stacks", description = "Number of stacks of a single item or block that a storage crate will hold.")
    public static int maximumStacks = 54;

    @Override
    public void register() {
        UseBlockCallback.EVENT.register(this::handleUseBlock);
        BLOCK_ENTITY = RegistryHandler.blockEntity(ID, StorageCrateBlockEntity::new);

        VanillaVariantMaterial.getTypes().forEach(material -> {
            registerStorageCrate(this, material);
        });
    }

    public static StorageCrateBlock registerStorageCrate(CharmModule module, IVariantMaterial material) {
        StorageCrateBlock crate = new StorageCrateBlock(module, material);
        STORAGE_CRATE_BLOCKS.put(material, crate);
        RegistryHelper.addBlocksToBlockEntity(BLOCK_ENTITY, crate);
        return crate;
    }

    private ActionResult handleUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof StorageCrateBlock))
            return ActionResult.PASS;

        ItemStack held = player.getStackInHand(hand);
        boolean isCreative = player.getAbilities().creativeMode;
        boolean isSneaking = player.isSneaking();

        Direction facing = world.getBlockState(pos).get(StorageCrateBlock.FACING);

        BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
        if (blockEntity instanceof StorageCrateBlockEntity) {
            StorageCrateBlockEntity crate = (StorageCrateBlockEntity) blockEntity;
            Interaction interacted = null;

            if (!world.isClient) {
                if (crate.count > 0 && (isSneaking || held.isEmpty())) {
                    int amountToRemove = Math.min(crate.item.getMaxCount(), crate.count);

                    if (!isCreative) {
                        PlayerHelper.addOrDropStack(player, new ItemStack(crate.item, amountToRemove));
                    }

                    crate.count -= amountToRemove;

                    if (crate.count == 0)
                        crate.item = null;

                    interacted = Interaction.REMOVED;

                } else if (!held.isEmpty()) {
                    if (crate.item == held.getItem() || crate.count == 0) {
                        crate.item = held.getItem();

                        int maxCount = maximumStacks * crate.item.getMaxCount();

                        if (crate.count >= maxCount) {
                            crate.count = maxCount;
                            interacted = Interaction.FILLED;
                        } else {
                            int amountToAdd = Math.min(maxCount - crate.count, held.getCount());
                            crate.count += amountToAdd;

                            if (!isCreative)
                                held.decrement(amountToAdd);

                            interacted = Interaction.ADDED;
                        }
                    }
                }

                if (interacted != null) {
                    crate.markDirty();
                    crate.sync();

                    PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
                    data.writeEnumConstant(interacted);
                    data.writeEnumConstant(facing);
                    data.writeLong(pos.asLong());
                    ServerPlayNetworking.send((ServerPlayerEntity) player, MSG_CLIENT_INTERACTED_WITH_CRATE, data);
                }
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    public enum Interaction {
        ADDED,
        REMOVED,
        FILLED
    }
}
