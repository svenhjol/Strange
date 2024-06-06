package svenhjol.strange.feature.travel_journals.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record BookmarkExtraData(String author, long timestamp, DyeColor color) {
    public static final Codec<BookmarkExtraData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("author")
            .forGetter(BookmarkExtraData::author),
        Codec.LONG.fieldOf("timestamp")
            .forGetter(BookmarkExtraData::timestamp),
        DyeColor.CODEC.fieldOf("color")
            .forGetter(BookmarkExtraData::color)
    ).apply(instance, BookmarkExtraData::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, BookmarkExtraData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
            BookmarkExtraData::author,
        ByteBufCodecs.VAR_LONG,
            BookmarkExtraData::timestamp,
        DyeColor.STREAM_CODEC,
            BookmarkExtraData::color,
        BookmarkExtraData::new
    );
    
    public static final BookmarkExtraData EMPTY = new BookmarkExtraData("", -1, DyeColor.WHITE);
}
