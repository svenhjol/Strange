package svenhjol.strange.module.scrolls;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ScrollHelper {
    public static final UUID ANY_UUID = UUID.fromString("0-0-0-0-1");
    public static final ResourceLocation FALLBACK_DIMENSION = new ResourceLocation("minecraft", "overworld");
    public static final int DEFAULT_QUEST_EXPIRY = 300000; // in minutes. roughly a month
    public static final int MAX_PLAYER_QUESTS = 5; // maybe this could be configurable?
}
