package svenhjol.strange.runicfragments;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.runestones.RunestonesHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class RunicFragmentItem extends CharmItem {
    public static final String DIMENSION_TAG = "dimension";
    public static final String POS_TAG = "pos";
    public static final String TRIED_TAG = "tried";

    public RunicFragmentItem(CharmModule module) {
        super(module, "runic_fragment", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);

        if (!world.isClient) {
            if (getPos(held) == null) {
                boolean result = populate(held, world, user.getBlockPos(), world.random);
                if (!result)
                    return TypedActionResult.fail(held);
            }

            user.getItemCooldownManager().set(this, 10);

            if (!ModuleHandler.enabled("strange:runic_altars")) {
                if (user.isSneaking() && isPopulated(held) && isCorrectDimension(held, world)) {
                    BlockPos destination = getNormalizedPos(held, world);
                    PlayerHelper.teleport(world, destination, user);
                    held.decrement(1);
                    return TypedActionResult.success(held);
                }
            }

            user.sendMessage(new TranslatableText("runic_fragment.strange.fragment_symbol", held.getName()), true);
        }

        return super.use(world, user, hand);
    }

    public static boolean populate(ItemStack fragment, World world, BlockPos startPos, Random random) {
        // don't try populating again until after timeout
        if (isWaitingForTimeout(fragment, world))
            return false;

        if (isPopulated(fragment))
            return false;

        if (!isCorrectDimension(fragment, world))
            return false;

        if (world.isClient)
            return false;

        ServerWorld serverWorld = (ServerWorld)world;

        Identifier locationId = RunicFragments.destinations.get(random.nextInt(RunicFragments.destinations.size()));
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(locationId);

        if (structureFeature == null) {
            Charm.LOG.warn("Unable to find structure " + locationId + ", setting timeout");
            RunicFragmentItem.setTried(fragment, serverWorld.getTime());
            return false;
        }

        startPos = PosHelper.addRandomOffset(startPos, random, 2000, 4000);
        BlockPos foundPos = serverWorld.locateStructure(structureFeature, startPos, 500, false);

        if (foundPos == null) {
            Charm.LOG.warn("Unable to find position of structure in this dimension, setting timeout");
            RunicFragmentItem.setTried(fragment, serverWorld.getTime());
            return false;
        }

        setPos(fragment, foundPos);
        setDimension(fragment, DimensionHelper.getDimension(serverWorld));
        setTried(fragment, 0);

        Text label;
        if (locationId.getPath().equals("foundation")) {
            label = new TranslatableText("structure.strange.foundation");
        } else if (locationId.getPath().equals("ruin")) {
            label = new TranslatableText("structure.strange.ruin");
        } else {
            label = new LiteralText(RunestonesHelper.getFormattedLocationName(locationId));
        }
        fragment.setCustomName(label);

        return true;
    }

    public static boolean isPopulated(ItemStack fragment) {
        return getPos(fragment) != null && getDimension(fragment) != null;
    }

    public static boolean isWaitingForTimeout(ItemStack fragment, World world) {
        long tried = getTried(fragment);
        return tried > 0 && world.getTime() - tried < 600;
    }

    public static boolean isCorrectDimension(ItemStack fragment, World world) {
        Identifier dimension = getDimension(fragment);
        return dimension == null || DimensionHelper.isDimension(world, dimension);
    }

    @Nullable
    public static BlockPos getNormalizedPos(ItemStack fragment, World world) {
        if (!isPopulated(fragment))
            return null;

        BlockPos pos = getPos(fragment);

        // recalculate position if it's supposed to be at the surface
        if (pos != null && pos.getY() == 0)
            pos = PosHelper.getSurfacePos(world, pos);

        return pos;
    }

    @Nullable
    public static Identifier getDimension(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(DIMENSION_TAG))
            return null;

        return new Identifier(fragment.getOrCreateTag().getString(DIMENSION_TAG));
    }

    @Nullable
    public static BlockPos getPos(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(fragment.getOrCreateTag().getLong(POS_TAG));
    }

    public static long getTried(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(TRIED_TAG))
            return 0;

        return fragment.getOrCreateTag().getLong(TRIED_TAG);
    }

    public static void setPos(ItemStack fragment, BlockPos pos) {
        fragment.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }

    public static void setDimension(ItemStack fragment, Identifier dimension) {
        fragment.getOrCreateTag().putString(DIMENSION_TAG, dimension.toString());
    }

    public static void setTried(ItemStack fragment, long ticks) {
        fragment.getOrCreateTag().putLong(TRIED_TAG, ticks);
    }
}
