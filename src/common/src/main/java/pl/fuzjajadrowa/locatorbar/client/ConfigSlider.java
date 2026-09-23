package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import java.util.function.Function;

final class ConfigSlider extends AbstractSliderButton {
    private final Component label;
    private final float min;
    private final float max;
    private final float step;
    private final Consumer<Float> onChange;
    private final Function<Float, String> valueText;

    ConfigSlider(
            int x,
            int y,
            int width,
            int height,
            Component label,
            float min,
            float max,
            float step,
            float initial,
            Consumer<Float> onChange,
            Function<Float, String> valueText
    ) {
        super(x, y, width, height, Component.empty(), toNormalized(snap(initial, min, max, step), min, max));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.onChange = onChange;
        this.valueText = valueText;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        float value = currentValue();
        setMessage(Component.translatable("locatorbar.config.slider_value", label, valueText.apply(value)));
    }

    @Override
    protected void applyValue() {
        float snapped = snap(fromNormalized(this.value, min, max), min, max, step);
        this.value = toNormalized(snapped, min, max);
        onChange.accept(snapped);
        updateMessage();
    }

    private float currentValue() {
        return snap(fromNormalized(this.value, min, max), min, max, step);
    }

    private static double toNormalized(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    private static float fromNormalized(double normalized, float min, float max) {
        return (float) (min + (max - min) * normalized);
    }

    private static float snap(float value, float min, float max, float step) {
        float clamped = Math.max(min, Math.min(max, value));
        return Math.round(clamped / step) * step;
    }
}