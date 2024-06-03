package svenhjol.strange.feature.runestones.common;

import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.charmony.event.EntityJoinEvent;
import svenhjol.charm.charmony.event.ServerStartEvent;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class Registers extends RegisterHolder<Runestones> {
    public static final String STONE_ID = "stone_runestone";
    public static final String BLACKSTONE_ID = "blackstone_runestone";
    public static final String OBSIDIAN_ID = "obsidian_runestone";

    public final Supplier<BlockEntityType<RunestoneBlockEntity>> blockEntity;
    public final Supplier<RunestoneBlock> stoneBlock;
    public final Supplier<RunestoneBlock> blackstoneBlock;
    public final Supplier<RunestoneBlock> obsidianBlock;
    public final List<Supplier<RunestoneBlock.BlockItem>> blockItems = new LinkedList<>();

    public Registers(Runestones feature) {
        super(feature);
        var registry = feature.registry();

        blockEntity = registry.blockEntity("runestone", () -> RunestoneBlockEntity::new);

        stoneBlock = registry.block(STONE_ID, RunestoneBlock::new);
        blackstoneBlock = registry.block(BLACKSTONE_ID, RunestoneBlock::new);
        obsidianBlock = registry.block(OBSIDIAN_ID, RunestoneBlock::new);

        blockItems.add(registry.item(STONE_ID, () -> new RunestoneBlock.BlockItem(stoneBlock)));
        blockItems.add(registry.item(BLACKSTONE_ID, () -> new RunestoneBlock.BlockItem(blackstoneBlock)));
        blockItems.add(registry.item(OBSIDIAN_ID, () -> new RunestoneBlock.BlockItem(obsidianBlock)));

        // Server packet senders
        registry.serverPacketSender(Networking.S2CWorldSeed.TYPE,
            Networking.S2CWorldSeed.CODEC);
        registry.serverPacketSender(Networking.S2CSacrificeInProgress.TYPE,
            Networking.S2CSacrificeInProgress.CODEC);
        registry.serverPacketSender(Networking.S2CActivateRunestone.TYPE,
            Networking.S2CActivateRunestone.CODEC);
    }

    @Override
    public void onEnabled() {
        ServerStartEvent.INSTANCE.handle(feature().handlers::serverStart);
        EntityJoinEvent.INSTANCE.handle(feature().handlers::entityJoin);
    }
}
