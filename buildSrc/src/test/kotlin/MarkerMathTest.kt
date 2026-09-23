import kotlin.test.*
import pl.fuzjajadrowa.locatorbar.util.MarkerMath

class MarkerMathTest {
    @Test fun wrapAndQuantization() {
        assertEquals(-180f, MarkerMath.wrapTo180(180f))
        assertEquals(-180f, MarkerMath.wrapTo180(-540f))
        assertEquals(0f, MarkerMath.wrapTo180(720f))
        assertEquals(179f, MarkerMath.wrapTo180(-181f))
        assertEquals(1.5f, MarkerMath.quantizeToHalfPixel(1.26f))
        assertEquals(-1.5f, MarkerMath.quantizeToHalfPixel(-1.26f))
    }

    @Test fun alphaBoundariesAndMonotonicity() {
        fun alpha(distance: Float) = MarkerMath.playerAlpha(distance, 50f, 125f, 500f, 0.4f)
        assertEquals(1f, alpha(0f))
        assertEquals(1f, alpha(50f))
        assertEquals(0.4f, alpha(125f), 0.00001f)
        assertEquals(0.4f, alpha(499f))
        assertEquals(0f, alpha(500f))
        var previous = 1f
        for (distance in 0..501) {
            val current = alpha(distance.toFloat())
            assertTrue(current >= 0f && current <= previous + 0.00001f)
            previous = current
        }
        assertEquals(1f, MarkerMath.playerAlpha(50f, 50f, 50f, 50f, 0.4f))
        assertEquals(0f, MarkerMath.playerAlpha(51f, 50f, 50f, 50f, 0.4f))
    }
}