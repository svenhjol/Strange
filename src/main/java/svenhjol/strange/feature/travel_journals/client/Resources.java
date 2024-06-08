package svenhjol.strange.feature.travel_journals.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.Strange;

public final class Resources {
    public static final Pair<Integer, Integer> BACKGROUND_DIMENSIONS = Pair.of(256, 208);
    public static final ResourceLocation BACKGROUND = Strange.id("textures/gui/travel_journal.png");
    public static final ResourceLocation PHOTO_BACKGROUND = Strange.id("textures/gui/photo_background.png");
    public static final Component BOOKMARKS = Component.translatable("gui.strange.travel_journals.bookmarks");
    public static final Component DESCRIPTION = Component.translatable("gui.strange.travel_journals.description");
    public static final Component DETAILS = Component.translatable("gui.strange.travel_journals.details");
    public static final Component EDIT_DESCRIPTION = Component.translatable("gui.strange.travel_journals.edit_description");
    public static final Component EDIT_NAME = Component.translatable("gui.strange.travel_journals.edit_name");
    public static final Component EXPORT_BOOKMARK = Component.translatable("gui.strange.travel_journals.export_bookmark");
    public static final Component EXPORT_MAP = Component.translatable("gui.strange.travel_journals.export_map");
    public static final Component NAME_TEXT = Component.translatable("gui.strange.travel_journals.name");
    public static final Component NEW_BOOKMARK = Component.translatable("gui.strange.travel_journals.new_bookmark");
    public static final Component TRAVEL_JOURNAL = Component.translatable("gui.strange.travel_journals.travel_journal");
    public static final String DIMENSION = "gui.strange.travel_journals.dimension";
    public static final String POSITION = "gui.strange.travel_journals.position";
    public static final String COORDINATES_KEY = "gui.strange.travel_journals.coordinates";
    public static final String CREATED_BY_KEY = "gui.strange.travel_journals.created_by";
}
