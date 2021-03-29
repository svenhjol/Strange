package svenhjol.strange.runestones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.stonecircles.StoneCircles;

public class RunestoneDustItem extends CharmItem {
    public RunestoneDustItem(CharmModule module) {
        super(module, "runestone_dust", new FabricItemSettings()
            .group(ItemGroup.MISC)
            .maxCount(64));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!DimensionHelper.isOverworld(world))
            return TypedActionResult.fail(stack);

        if (!ModuleHandler.enabled("strange:stone_circles"))
            return TypedActionResult.fail(stack);

        if (!player.isCreative())
            stack.decrement(1);

        int x = player.getBlockPos().getX();
        int y = player.getBlockPos().getY();
        int z = player.getBlockPos().getZ();

        player.getItemCooldownManager().set(this, 40);

        // client
        if (world.isClient) {
            player.swingHand(hand);
            world.playSound(player, x, y, z, SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        // server
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld)world;
            BlockPos pos = serverWorld.locateStructure(StoneCircles.STONE_CIRCLE_STRUCTURE, player.getBlockPos(), 1500, false);
            if (pos != null) {
                RunestoneDustEntity entity = new RunestoneDustEntity(world, pos.getX(), pos.getZ());
                Vec3d look = player.getRotationVector();

                entity.setPos(x + look.x * 2, y + 0.5, z + look.z * 2);
                world.spawnEntity(entity);
                return TypedActionResult.pass(stack);
            }
        }

        return TypedActionResult.success(stack);
    }
}
