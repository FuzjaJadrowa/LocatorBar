package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarOffset;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LocatorBarConfigScreen extends Screen {
    private static final int TOTAL_PAGES = 1;
    private static final float SCALE_MIN = 0.5F;
    private static final float SCALE_MAX = 2.0F;
    private static final float SCALE_STEP = 0.05F;
    private static final float VIEW_ANGLE_MIN = 30.0F;
    private static final float VIEW_ANGLE_MAX = 180.0F;
    private static final float VIEW_ANGLE_STEP = 5.0F;

    private final Screen parent;
    private LocatorBarStyle selectedStyle;
    private float selectedScale;
    private LocatorBarOffset selectedOffset;
    private float selectedViewAngle;
    private boolean selectedShowCoordinates;
    private CoordinatesFormat selectedCoordinatesFormat;
    private int page = 0;
    private Button styleButton;
    private Button offsetButton;
    private Button showCoordinatesButton;
    private Button coordinatesFormatButton;

    public LocatorBarConfigScreen(Screen parent) {
        super(Component.literal("Locator bar config"));
        this.parent = parent;
        this.selectedStyle = LocatorBarConfig.getStyle();
        this.selectedScale = LocatorBarConfig.getScale();
        this.selectedOffset = LocatorBarConfig.getOffset();
        this.selectedViewAngle = LocatorBarConfig.getViewAngle();
        this.selectedShowCoordinates = LocatorBarConfig.isShowCoordinates();
        this.selectedCoordinatesFormat = LocatorBarConfig.getCoordinatesFormat();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int controlsY = this.height - 28;
        int controlX = centerX + 20;

        styleButton = Button.builder(styleButtonText(), button -> cycleStyle())
                .bounds(controlX, 54, 120, 20)
                .build();
        addRenderableWidget(styleButton);

        addRenderableWidget(
                new ConfigSlider(
                        controlX,
                        82,
                        120,
                        20,
                        Component.literal("Scale"),
                        SCALE_MIN,
                        SCALE_MAX,
                        SCALE_STEP,
                        selectedScale,
                        value -> selectedScale = value,
                        value -> String.format(Locale.ROOT, "%.2fx", value)
                )
        );

        offsetButton = Button.builder(offsetButtonText(), button -> cycleOffset())
                .bounds(controlX, 108, 120, 20)
                .build();
        addRenderableWidget(offsetButton);

        addRenderableWidget(
                new ConfigSlider(
                        controlX,
                        134,
                        120,
                        20,
                        Component.literal("View angle"),
                        VIEW_ANGLE_MIN,
                        VIEW_ANGLE_MAX,
                        VIEW_ANGLE_STEP,
                        selectedViewAngle,
                        value -> selectedViewAngle = value,
                        value -> Integer.toString(Math.round(value)) + "\u00b0"
                )
        );

        showCoordinatesButton = Button.builder(showCoordinatesButtonText(), button -> toggleShowCoordinates())
                .bounds(controlX, 160, 120, 20)
                .build();
        addRenderableWidget(showCoordinatesButton);

        coordinatesFormatButton = Button.builder(coordinatesFormatButtonText(), button -> cycleCoordinatesFormat())
                .bounds(controlX, 186, 120, 20)
                .build();
        addRenderableWidget(coordinatesFormatButton);

        addRenderableWidget(
                Button.builder(Component.literal("Close"), button -> onClose())
                        .bounds(centerX - 150, controlsY, 70, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.literal("Save"), button -> saveAndClose())
                        .bounds(centerX - 35, controlsY, 70, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.literal("<"), button -> previousPage())
                        .bounds(centerX + 80, controlsY, 30, 20)
                        .build()
        ).active = false;

        addRenderableWidget(
                Button.builder(Component.literal(">"), button -> nextPage())
                        .bounds(centerX + 116, controlsY, 30, 20)
                        .build()
        ).active = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int leftLabelX = centerX - 124;
        guiGraphics.drawCenteredString(this.font, Component.literal("Locator bar config"), centerX, 14, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.literal("1. General"), centerX, 34, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("Locator bar style"), leftLabelX, 60, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Locator bar scale"), leftLabelX, 88, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Locator bar offset"), leftLabelX, 114, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("View angle"), leftLabelX, 140, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Show coordinates"), leftLabelX, 166, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Coordinates format"), leftLabelX, 192, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Page 1/1"), centerX + 91, this.height - 40, 0xA0A0A0, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void saveAndClose() {
        LocatorBarConfig.setStyle(selectedStyle);
        LocatorBarConfig.setScale(selectedScale);
        LocatorBarConfig.setOffset(selectedOffset);
        LocatorBarConfig.setViewAngle(selectedViewAngle);
        LocatorBarConfig.setShowCoordinates(selectedShowCoordinates);
        LocatorBarConfig.setCoordinatesFormat(selectedCoordinatesFormat);
        LocatorBarConfig.save();
        onClose();
    }

    private void cycleStyle() {
        selectedStyle = selectedStyle.next();
        if (styleButton != null) {
            styleButton.setMessage(styleButtonText());
        }
    }

    private Component styleButtonText() {
        return Component.literal(selectedStyle.label());
    }

    private void cycleOffset() {
        selectedOffset = selectedOffset.next();
        if (offsetButton != null) {
            offsetButton.setMessage(offsetButtonText());
        }
    }

    private Component offsetButtonText() {
        return Component.literal(selectedOffset.label());
    }

    private void toggleShowCoordinates() {
        selectedShowCoordinates = !selectedShowCoordinates;
        if (showCoordinatesButton != null) {
            showCoordinatesButton.setMessage(showCoordinatesButtonText());
        }
    }

    private Component showCoordinatesButtonText() {
        return Component.literal(selectedShowCoordinates ? "On" : "Off");
    }

    private void cycleCoordinatesFormat() {
        selectedCoordinatesFormat = selectedCoordinatesFormat.next();
        if (coordinatesFormatButton != null) {
            coordinatesFormatButton.setMessage(coordinatesFormatButtonText());
        }
    }

    private Component coordinatesFormatButtonText() {
        return Component.literal(selectedCoordinatesFormat.label());
    }

    private void previousPage() {
        page = Math.max(0, page - 1);
    }

    private void nextPage() {
        page = Math.min(TOTAL_PAGES - 1, page + 1);
    }

    private static final class ConfigSlider extends AbstractSliderButton {
        private final Component label;
        private final float min;
        private final float max;
        private final float step;
        private final Consumer<Float> onChange;
        private final Function<Float, String> valueText;

        private ConfigSlider(
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
            this.onChange.accept(currentValue());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            float value = currentValue();
            setMessage(Component.literal(label.getString() + ": " + valueText.apply(value)));
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
}
