package svenhjol.strange.feature.travel_journals;

import net.minecraft.util.Mth;
import svenhjol.charm.charmony.annotation.Configurable;
import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.feature.travel_journals.common.Advancements;
import svenhjol.strange.feature.travel_journals.common.Handlers;
import svenhjol.strange.feature.travel_journals.common.Networking;
import svenhjol.strange.feature.travel_journals.common.Registers;

@Feature(description = """
    Travel journals are craftable items to store photos and descriptions of interesting places in the world.
    Each journal contains bookmarked locations. Locations can be saved to pages and added to other journals.
    Press the bookmark key (defaults to B) with a journal in your inventory to add a new bookmark.""")
public final class TravelJournals extends CommonFeature {
    public static final String PHOTOS_DIR = "strange_travel_journal_photos";
    
    public final Registers registers;
    public final Networking networking;
    public final Handlers handlers;
    public final Advancements advancements;

    @Configurable(
        name = "Number of bookmarks per journal",
        description = """
            Maximum number of bookmarks that can be added to a single travel journal.
            Setting too high a number could impact network performance.
            """
    )
    private static final int numberOfBookmarksPerJournal = 50;
    
    @Configurable(
        name = "Scaled photo width",
        description = """
            Width (in pixels) that photos will be scaled down to before being uploaded to the server.
            This affects the storage size of the world data and network packet size when a client wants
            to download a copy of the photo. Smaller sizes optimize space and speed but reduce quality.
            The scaled width should be double the scaled height."""
    )
    private static final int scaledPhotoWidth = 192;
    
    @Configurable(
        name = "Scaled photo height",
        description = """
            Height (in pixels) that photos will be scaled down to before being uploaded to the server.
            This affects the storage size of the world data and network packet size when a client wants
            to download a copy of the photo. Smaller sizes optimize space and speed but reduce quality.
            The scaled height should be half the scaled width."""
    )
    private static final int scaledPhotoHeight = 96;

    public TravelJournals(CommonLoader loader) {
        super(loader);

        registers = new Registers(this);
        networking = new Networking(this);
        handlers = new Handlers(this);
        advancements = new Advancements(this);
    }
    
    public int numberOfBookmarksPerJournal() {
        return Mth.clamp(1, 1000, numberOfBookmarksPerJournal);
    }
    
    public int scaledPhotoWidth() {
        return Mth.clamp(scaledPhotoWidth, 64, 2048);
    }
    
    public int scaledPhotoHeight() {
        return Mth.clamp(scaledPhotoHeight, 64, 2048);
    }
}
