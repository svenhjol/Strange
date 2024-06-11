package svenhjol.strange.feature.runestones.common;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.charmony.event.EntityJoinEvent;
import svenhjol.charm.charmony.event.PlayerTickEvent;
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
    public final Supplier<SoundEvent> activateSound;
    public final Supplier<SoundEvent> fizzleItemSound;
    public final Supplier<SoundEvent> powerUpSound;
    public final Supplier<SoundEvent> travelSound;

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
        registry.serverPacketSender(Networking.S2CTeleportedLocation.TYPE,
            Networking.S2CTeleportedLocation.CODEC);
        
        // Client packet senders
        registry.clientPacketSender(Networking.C2SLookingAtRunestone.TYPE,
            Networking.C2SLookingAtRunestone.CODEC);
        
        // Receivers of packets sent from client
        registry.packetReceiver(Networking.C2SLookingAtRunestone.TYPE,
            () -> feature.handlers::lookingAtRunestoneReceived);

        // Sound effects
        activateSound = registry.soundEvent("runestone_activate");
        fizzleItemSound = registry.soundEvent("runestone_fizzle_item");
        powerUpSound = registry.soundEvent("runestone_power_up");
        travelSound = registry.soundEvent("runestone_travel");
    }

    @Override
    public void onEnabled() {
        ServerStartEvent.INSTANCE.handle(feature().handlers::serverStart);
        EntityJoinEvent.INSTANCE.handle(feature().handlers::entityJoin);
        PlayerTickEvent.INSTANCE.handle(feature().handlers::playerTick);
    }
}
