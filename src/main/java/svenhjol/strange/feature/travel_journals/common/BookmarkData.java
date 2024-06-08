package svenhjol.strange.feature.travel_journals.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record BookmarkData(UUID id, String name, ResourceKey<Level> dimension, BlockPos pos, BookmarkExtraData extra) {
    public static final Codec<BookmarkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id")
            .forGetter(BookmarkData::id),
        Codec.STRING.fieldOf("name")
            .forGetter(BookmarkData::name),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension")
            .forGetter(BookmarkData::dimension),
        BlockPos.CODEC.fieldOf("pos")
            .forGetter(BookmarkData::pos),
        BookmarkExtraData.CODEC.fieldOf("extra")
            .forGetter(BookmarkData::extra)
    ).apply(instance, BookmarkData::new));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, BookmarkData> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
            BookmarkData::id,
        ByteBufCodecs.STRING_UTF8,
            BookmarkData::name,
        ResourceKey.streamCodec(Registries.DIMENSION),
            BookmarkData::dimension,
        BlockPos.STREAM_CODEC,
            BookmarkData::pos,
        BookmarkExtraData.STREAM_CODEC,
            BookmarkData::extra,
        BookmarkData::new
    );
    
    public static final BookmarkData EMPTY = new BookmarkData(
        UUID.randomUUID(),
        "",
        Level.OVERWORLD,
        BlockPos.ZERO,
        BookmarkExtraData.EMPTY
    );
    
    public static Mutable create() {
        return new Mutable(UUID.randomUUID(), EMPTY);
    }
    
    public static class Mutable {
        private final UUID id;
        private String description;
        private String name;
        private ResourceKey<Level> dimension;
        private BlockPos pos;
        private String author;
        private long timestamp;
        private DyeColor color;
        
        public Mutable(BookmarkData data) {
            this(data.id(), data);
        }
        
        public Mutable(UUID id, BookmarkData data) {
            this.id = id;
            this.name = data.name();
            this.dimension = data.dimension();
            this.pos = data.pos();
            this.author = data.extra().author();
            this.description = data.extra().description();
            this.timestamp = data.extra().timestamp();
            this.color = data.extra().color();
        }
        
        public UUID id() {
            return this.id;
        }
        
        public String name() {
            return this.name;
        }

        public String description() {
            return this.description;
        }
        
        public BlockPos pos() {
            return this.pos;
        }
        
        public ResourceKey<Level> dimension() {
            return this.dimension;
        }
        
        public DyeColor color() {
            return this.color;
        }
        
        public Mutable name(String name) {
            this.name = name;
            return this;
        }
        
        public Mutable pos(BlockPos pos) {
            this.pos = pos;
            return this;
        }
        
        public Mutable dimension(ResourceKey<Level> dimension) {
            this.dimension = dimension;
            return this;
        }
        
        public Mutable author(String author) {
            this.author = author;
            return this;
        }
        
        public Mutable description(String description) {
            this.description = description;
            return this;
        }
        
        public Mutable timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Mutable color(DyeColor color) {
            this.color = color;
            return this;
        }
        
        public BookmarkData toImmutable() {
            var extra = new BookmarkExtraData(author, description, timestamp, color);
            return new BookmarkData(id, name, dimension, pos, extra);
        }
    }
}
