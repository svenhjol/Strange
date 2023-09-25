package svenhjol.strange.feature.ambient_music_discs;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;

public class AmbientRecordItem extends RecordItem {
    public AmbientRecordItem(SoundEvent sound) {
        super(1, sound, new Properties().stacksTo(1).rarity(Rarity.RARE), 1200);
    }
}
