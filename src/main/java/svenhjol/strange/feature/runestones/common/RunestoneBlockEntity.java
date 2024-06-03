package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.common.block.entity.CharmBlockEntity;
import svenhjol.charm.charmony.helper.PlayerHelper;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.Runestones;

public class RunestoneBlockEntity extends CharmBlockEntity<Runestones> {
    public static final Runestones RUNESTONES = Resolve.feature(Runestones.class);
    public static final String LOCATION_TAG = "location";
    public static final String SACRIFICE_TAG = "sacrifice";
    public static final int MAX_SACRIFICE_CHECKS = 10;


    // TODO: get+set for these
    public RunestoneLocation location;
    public ItemStack sacrifice;
    public int sacrificeChecks = 0;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Resolve.feature(Runestones.class).registers.blockEntity.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(LOCATION_TAG)) {
            this.location = RunestoneLocation.load(tag.getCompound(LOCATION_TAG));
        }
        if (tag.contains(SACRIFICE_TAG)) {
            this.sacrifice = ItemStack.parse(provider, tag.getCompound(SACRIFICE_TAG)).orElseThrow();
        }


        // TODO: other tag data
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (location != null) {
            tag.put(LOCATION_TAG, location.save());
        }
        if (sacrifice != null) {
            tag.put(SACRIFICE_TAG, sacrifice.save(provider));
        }

        // TODO: other tag data
    }

    @Override
    public Class<Runestones> typeForFeature() {
        return Runestones.class;
    }

    public boolean isActivated() {
        var state = getBlockState();
        return state.getValue(RunestoneBlock.ACTIVATED);
    }

    public void activate(Level level, BlockPos pos, BlockState state) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }

        state = state.setValue(RunestoneBlock.ACTIVATED, true);
        level.setBlock(pos, state, 3);
        // level.playSound(null, itemPos, Runestones.activateSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        sacrificeChecks = 0;
        setChanged();

        // Activation effect for all nearby players.
        PlayerHelper.getPlayersInRange(level, pos, 8.0d)
            .forEach(player -> Networking.S2CActivateRunestone.send((ServerPlayer)player, pos));
    }

    public boolean isValid() {
        return location != null && sacrifice != null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RunestoneBlockEntity runestone) {
        if (!runestone.isActivated() && runestone.isValid() && level.getGameTime() % 10 == 0) {
            ItemEntity foundItem = null;
            var itemEntities = level.getEntitiesOfClass(ItemEntity.class, (new AABB(pos)).inflate(3.2d));
            for (var itemEntity : itemEntities) {
                if (itemEntity.getItem().is(runestone.sacrifice.getItem())) {
                    foundItem = itemEntity;
                    break;
                }
            }
            if (foundItem != null) {
                var itemPos = foundItem.position();

                // Add particle effect around the item to be consumed. This needs to be done via network packet.
                PlayerHelper.getPlayersInRange(level, pos, 8.0d)
                    .forEach(player -> Networking.S2CSacrificeInProgress.send((ServerPlayer)player, pos, itemPos));

                // Increase the number of checks. If maximum, consume the item and activate the runestone.
                runestone.sacrificeChecks++;
                if (runestone.sacrificeChecks >= MAX_SACRIFICE_CHECKS) {
                    var stack = foundItem.getItem();
                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                    } else {
                        foundItem.discard();
                    }
                    runestone.activate(level, pos, state);
                }
            } else {
                runestone.sacrificeChecks = 0;
            }
        }
    }
}
