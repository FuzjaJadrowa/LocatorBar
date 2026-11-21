package pl.fuzjajadrowa.locatorbar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkerMathTest {
    @Test
    void wrapTo180NormalizesAngles() {
        assertEquals(0.0F, MarkerMath.wrapTo180(0.0F), 1.0E-5F);
        assertEquals(-180.0F, MarkerMath.wrapTo180(180.0F), 1.0E-5F);
        assertEquals(-90.0F, MarkerMath.wrapTo180(270.0F), 1.0E-5F);
        assertEquals(90.0F, MarkerMath.wrapTo180(-270.0F), 1.0E-5F);
    }

    @Test
    void quantizeToHalfPixelRoundsToNearestHalf() {
        assertEquals(1.0F, MarkerMath.quantizeToHalfPixel(1.1F), 1.0E-5F);
        assertEquals(1.5F, MarkerMath.quantizeToHalfPixel(1.4F), 1.0E-5F);
        assertEquals(2.0F, MarkerMath.quantizeToHalfPixel(1.8F), 1.0E-5F);
    }

    @Test
    void playerAlphaRespectsFadeAndHideThresholds() {
        assertEquals(1.0F, MarkerMath.playerAlpha(25.0F, 50.0F, 150.0F, 300.0F, 0.4F), 1.0E-5F);
        assertEquals(0.4F, MarkerMath.playerAlpha(150.0F, 50.0F, 150.0F, 300.0F, 0.4F), 1.0E-5F);
        assertEquals(0.4F, MarkerMath.playerAlpha(250.0F, 50.0F, 150.0F, 300.0F, 0.4F), 1.0E-5F);
        assertEquals(0.0F, MarkerMath.playerAlpha(300.0F, 50.0F, 150.0F, 300.0F, 0.4F), 1.0E-5F);
    }
}