package svenhjol.strange.feature.piglin_pointing;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.piglin.Piglin;
import svenhjol.charmony.client.ClientFeature;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PiglinPointingClient extends ClientFeature {
    public static final Map<UUID, Float> POINT_ANIMATION = new WeakHashMap<>();

    public static void animate(PiglinModel<Piglin> model, Piglin piglin, float f, float g, float h, float i, float j) {
        var uuid = piglin.getUUID();

        if (PiglinPointing.isPointing(piglin)) {
            POINT_ANIMATION.putIfAbsent(uuid, 3f);

            var current = POINT_ANIMATION.get(uuid);
            if (current < 0) {
                if (current > -1.45f) {
                    // Do arm lifting animation.
                    POINT_ANIMATION.put(uuid, current - 0.037f);
                    model.leftArm.xRot = current;
                } else {
                    // Hold the arm at a fixed height.
                    model.leftArm.xRot = -1.45f;

                    // Move the arm back and forth a little
                    float n = h / 100.0f;
                    model.leftArm.z = Mth.sin(n * 30.0f) * 0.2f;
                }
            } else {
                // Some time before doing the lifting animation.
                POINT_ANIMATION.put(uuid, current - 0.035f);
            }

        } else if (POINT_ANIMATION.containsKey(uuid)) {

            var current = POINT_ANIMATION.get(uuid);
            if (current < 0f) {
                // Do arm dropping animation a bit faster than raising.
                POINT_ANIMATION.put(uuid, current + 0.063f);
                model.leftArm.xRot = current;
            } else {
                // Animation is finished, remove entity tracking.
                POINT_ANIMATION.remove(uuid);
            }
        }
    }
}
