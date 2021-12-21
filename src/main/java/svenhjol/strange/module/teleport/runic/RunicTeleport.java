package svenhjol.strange.module.teleport.runic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.api.event.ActivateRunestoneCallback;
import svenhjol.strange.module.bookmarks.BookmarkBranch;
import svenhjol.strange.module.discoveries.DiscoveryBranch;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runic_tomes.RunicTomeItem;
import svenhjol.strange.module.runic_tomes.event.ActivateRunicTomeCallback;
import svenhjol.strange.module.teleport.ITeleportType;
import svenhjol.strange.module.teleport.helper.TeleportHelper;
import svenhjol.strange.module.teleport.runic.handler.*;

public class RunicTeleport implements ITeleportType {
    @Override
    public void register() {}

    @Override
    public void runWhenEnabled() {
        // register the actions that can teleport a player
        ActivateRunestoneCallback.EVENT.register(this::handleActivateRunestone);
        ActivateRunicTomeCallback.EVENT.register(this::handleActivateRunicTome);
    }

    private void handleActivateRunestone(ServerPlayer player, BlockPos origin, String runes, ItemStack sacrifice) {
        if (!tryTeleport(player, runes, sacrifice, origin)) {
            LogHelper.warn(this.getClass(), "Runestone activation failed");
            TeleportHelper.explode((ServerLevel)player.level, origin, 2.0F, Explosion.BlockInteraction.BREAK);
        }
    }

    private void handleActivateRunicTome(ServerPlayer player, BlockPos origin, ItemStack tome, ItemStack sacrifice) {
        String runes = RunicTomeItem.getRunes(tome);
        if (!tryTeleport(player, runes, sacrifice, origin)) {
            LogHelper.warn(this.getClass(), "Runic tome activation failed");
            TeleportHelper.explode((ServerLevel)player.level, origin, 2.0F, Explosion.BlockInteraction.BREAK);
        }
    }

    private boolean tryTeleport(ServerPlayer player, String runes, ItemStack sacrifice, BlockPos origin) {
        BaseTeleportHandler<?> handler;
        ServerLevel level = (ServerLevel)player.level;
        RuneBranch<?, ?> branch = RuneHelper.branch(runes);
        if (branch == null) return false;

        handler = switch (branch.getBranchName()) {
            case BiomeBranch.NAME -> new BiomeTeleportHandler((BiomeBranch)branch);
            case BookmarkBranch.NAME -> new BookmarkTeleportHandler((BookmarkBranch)branch);
            case DimensionBranch.NAME -> new DimensionTeleportHandler((DimensionBranch)branch);
            case DiscoveryBranch.NAME -> new DiscoveryTeleportHandler((DiscoveryBranch)branch);
            case StructureBranch.NAME -> new StructureTeleportHandler((StructureBranch)branch);
            default -> null;
        };

        if (handler == null) {
            LogHelper.debug(this.getClass(), "No teleport handler for branch: " + branch.getBranchName());
            return false;
        }

        boolean result = handler.setup(level, player, sacrifice, runes, origin);
        if (!result) return false;

        handler.process();
        return true;
    }
}
