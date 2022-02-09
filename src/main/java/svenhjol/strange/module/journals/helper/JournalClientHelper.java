package svenhjol.strange.module.journals.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.Bookmarks;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.bookmarks.DefaultIcon;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.discoveries.DiscoveryClientHelper;
import svenhjol.strange.module.journals.JournalsClient;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class JournalClientHelper {
    public static List<Bookmark> getPlayerBookmarks() {
        var minecraft = Minecraft.getInstance();
        var branch = BookmarksClient.getBranch();
        if (branch.isEmpty()) return List.of();
        return branch.get().values(minecraft.player.getUUID());
    }

    public static void addBookmark() {
        if (getPlayerBookmarks().size() >= Bookmarks.maxBookmarksPerPlayer) {
            // TODO: some kind of message saying you've reached your limit
            return;
        }

        // Generate a name and icon for the bookmark. Try and use the current biome.
        var minecraft = Minecraft.getInstance();
        String name;
        ResourceLocation icon;

        var level = minecraft.player.level;
        var opt = level.getBiomeName(minecraft.player.blockPosition());

        if (opt.isPresent()) {
            String biomeName = StringHelper.snakeToPretty(opt.get().location().getPath(), true);
            name = I18n.get("gui.strange.journal.biome_bookmark", biomeName);
        } else {
            name = I18n.get("gui.strange.journal.bookmark");
        }

        icon = DefaultIcon.forDimension(level.dimension().location()).getId();
        BookmarksClient.SEND_CREATE_BOOKMARK.send(name, icon, true);
    }

    public static List<Discovery> getFilteredDiscoveries(boolean withIgnored) {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return List.of();

        var validDiscoveries = DiscoveryClientHelper.getDiscoveries();
        var discoveries = journal.getDiscoveries();
        var ignored = journal.getIgnoredDiscoveries();

        return validDiscoveries.stream()
            .filter(d -> d.getTime() == 0 || d.getPlayer() == null || discoveries.contains(d.getRunes()))
            .filter(d -> withIgnored || !ignored.contains(d.getRunes()))
            .filter(d -> JournalHelper.countUnknownRunes(d.getRunes(), journal) == 0)
            .collect(Collectors.toList());
    }

    public static boolean hasIgnoredAnyDiscoveries() {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return false;

        var ignored = journal.getIgnoredDiscoveries();
        return ignored.size() > 0;
    }
}
