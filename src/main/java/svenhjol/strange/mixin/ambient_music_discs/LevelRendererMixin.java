package svenhjol.strange.mixin.ambient_music_discs;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscsClient;
import svenhjol.strange.feature.ambient_music_discs.AmbientRecordItem;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    /**
     * We don't actually modify the argument, just capture the ID of the record's music.
     */
    @ModifyArg(
        method = "levelEvent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;byId(I)Lnet/minecraft/world/item/Item;"
        )
    )
    private int modifyCaptureSound(int i) {
        if (Item.byId(i) instanceof AmbientRecordItem record) {
            AmbientMusicDiscsClient.soundHolder = record.getSound().getLocation();
        }
        return i;
    }
}
