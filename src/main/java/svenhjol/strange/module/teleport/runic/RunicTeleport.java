package svenhjol.strange.module.teleport.runic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.ActivateRunestoneCallback;
import svenhjol.strange.init.StrangeParticles;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.RunicTomes;
import svenhjol.strange.module.runic_tomes.event.ActivateRunicTomeCallback;
import svenhjol.strange.module.teleport.iface.ITeleportType;
import svenhjol.strange.module.teleport.runic.handler.*;
import svenhjol.strange.module.teleport.runic.network.ServerSendRunicTeleportEffect;

public class RunicTeleport implements ITeleportType {
    public static ServerSendRunicTeleportEffect SEND_RUNIC_TELEPORT_EFFECT;

    @Override
    public void register() {
        SEND_RUNIC_TELEPORT_EFFECT = new ServerSendRunicTeleportEffect();
    }

    @Override
    public void runWhenEnabled() {
        // register the actions that can teleport a player
        ActivateRunestoneCallback.EVENT.register(this::handleActivateRunestone);
        ActivateRunicTomeCallback.EVENT.register(this::handleActivateRunicTome);
    }

    private void handleActivateRunestone(ServerPlayer player, BlockPos origin, String runes, ItemStack sacrifice) {
        var result = tryTeleport(player, runes, sacrifice, origin);

        // Outcomes are success, pass or fail.

        // On success trigger the advancement and send instructions to client to display particles.
        if (result == InteractionResult.SUCCESS) {
            Runestones.triggerActivatedRunestone(player);
            SEND_RUNIC_TELEPORT_EFFECT.send(player, origin, Type.RUNESTONE);
        }

        // On fail, something has gone badly wrong, so destroy the runestone.
        if (result == InteractionResult.FAIL) {
            LogHelper.warn(getClass(), "Runestone activation failed");
            WorldHelper.explode((ServerLevel) player.level, origin, 2.0F, Explosion.BlockInteraction.BREAK);
        }
    }

    private void handleActivateRunicTome(ServerPlayer player, BlockPos origin, ItemStack tome, ItemStack sacrifice) {
        String runes = RunicTomeItem.getRunes(tome);

        var result = tryTeleport(player, runes, sacrifice, origin);
        if (result == InteractionResult.SUCCESS) {
            RunicTomes.triggerActivateTome(player);

            // This allows the client to show particles for the runic tome.
            SEND_RUNIC_TELEPORT_EFFECT.send(player, origin, Type.RUNIC_TOME);
        }

        if (result == InteractionResult.FAIL) {
            LogHelper.warn(getClass(), "Runic tome activation failed");
            WorldHelper.explode((ServerLevel)player.level, origin, 2.0F, Explosion.BlockInteraction.BREAK);
        }
    }

    private InteractionResult tryTeleport(ServerPlayer player, String runes, ItemStack sacrifice, BlockPos origin) {
        BaseTeleportHandler<?> handler;
        ServerLevel level = (ServerLevel)player.level;

        // Skip if there's no matching branch for these runes.
        RuneBranch<?, ?> branch = RuneHelper.branch(runes);
        if (branch == null) return InteractionResult.PASS;

        // This module has handlers that deal with a bunch of different rune branches. Map here.
        handler = switch (branch.getBranchName()) {
            case BiomeBranch.NAME -> new BiomeTeleportHandler((BiomeBranch)branch);
            case BookmarkBranch.NAME -> new BookmarkTeleportHandler((BookmarkBranch)branch);
            case DimensionBranch.NAME -> new DimensionTeleportHandler((DimensionBranch)branch);
            case DiscoveryBranch.NAME -> new DiscoveryTeleportHandler((DiscoveryBranch)branch);
            case StructureBranch.NAME -> new StructureTeleportHandler((StructureBranch)branch);
            default -> null;
        };

        // Skip if there's no matching handler.
        if (handler == null) {
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "No teleport handler for branch: " + branch.getBranchName());
            return InteractionResult.PASS;
        }

        // Initialize the handler. If initialization fails it's because there's something wrong with the runes.
        // Return explicit fail to let the parent class handle the outcome.
        boolean result = handler.init(level, player, sacrifice, runes, origin);
        if (!result) return InteractionResult.FAIL;

        // The handler's process() method is delegated to the handler subclasses.
        // If process returns false it means that there was a soft fail of the teleport, for example, wrong item sacrifice.
        result = handler.process();

        if (result) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public enum Type {
        GENERIC(ParticleTypes.PORTAL),
        RUNESTONE(StrangeParticles.ILLAGERALT),
        RUNIC_TOME(StrangeParticles.ILLAGERALT),
        FAIL(ParticleTypes.SMOKE);

        private final ParticleOptions particle;

        Type(ParticleOptions particle) {
            this.particle = particle;
        }

        public ParticleOptions getParticle() {
            return particle;
        }
    }
}
