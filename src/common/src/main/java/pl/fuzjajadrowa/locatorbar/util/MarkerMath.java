package pl.fuzjajadrowa.locatorbar.util;

public final class MarkerMath {
    private MarkerMath() {}

    public static float wrapTo180(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        } else if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float quantizeToHalfPixel(float value) {
        return Math.round(value * 2.0F) / 2.0F;
    }

    public static float playerAlpha(float distance, float fadeStartDistance, float fadeToMinDistance,
                                    float hideDistance, float minAlpha) {
        if (distance <= fadeStartDistance) {
            return 1.0F;
        }
        if (distance <= fadeToMinDistance) {
            if (fadeToMinDistance <= fadeStartDistance) {
                return minAlpha;
            }
            float progress = (distance - fadeStartDistance) / (fadeToMinDistance - fadeStartDistance);
            float curvedProgress = (float) Math.pow(progress, 1.65D);
            return 1.0F - (curvedProgress * (1.0F - minAlpha));
        }
        if (distance < hideDistance) {
            return minAlpha;
        }
        return 0.0F;
    }
}