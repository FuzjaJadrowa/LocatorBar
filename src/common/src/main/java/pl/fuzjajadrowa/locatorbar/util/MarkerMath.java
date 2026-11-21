package pl.fuzjajadrowa.locatorbar.util;

public final class MarkerMath {
    public static final float BASE_HUD_FOV = 70.0F;

    public enum PitchDirection {
        NONE,
        UP,
        DOWN
    }

    private MarkerMath() {}

    public static PitchDirection calculatePitchDirection(
            double dx,
            double dy,
            double dz,
            float cameraYaw,
            float cameraPitch,
            float fov
    ) {
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        double forwardHoriz = -dx * Math.sin(yawRad) + dz * Math.cos(yawRad);
        double viewY = dy * Math.cos(pitchRad) + forwardHoriz * Math.sin(pitchRad);
        double viewDepth = forwardHoriz * Math.cos(pitchRad) - dy * Math.sin(pitchRad);

        double tanHalfFov = Math.tan(Math.toRadians(Math.max(1.0F, fov) / 2.0D));
        if (viewDepth <= 1.0E-4D) {
            if (viewY > 0.0D) {
                return PitchDirection.UP;
            }
            if (viewY < 0.0D) {
                return PitchDirection.DOWN;
            }
            return PitchDirection.NONE;
        }

        double normalizedY = viewY / (viewDepth * tanHalfFov);
        if (normalizedY < -1.0D) {
            return PitchDirection.DOWN;
        }
        if (normalizedY > 1.0D) {
            return PitchDirection.UP;
        }
        return PitchDirection.NONE;
    }

    public static PitchDirection calculateHorizonPitchDirection(float cameraPitch, float fov) {
        if (cameraPitch <= -90.0F) {
            return PitchDirection.DOWN;
        }
        if (cameraPitch >= 90.0F) {
            return PitchDirection.UP;
        }
        double tanHalfFov = Math.tan(Math.toRadians(Math.max(1.0F, fov) / 2.0D));
        double horizonY = Math.tan(Math.toRadians(cameraPitch)) / tanHalfFov;
        if (horizonY < -1.0D) {
            return PitchDirection.DOWN;
        }
        if (horizonY > 1.0D) {
            return PitchDirection.UP;
        }
        return PitchDirection.NONE;
    }

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