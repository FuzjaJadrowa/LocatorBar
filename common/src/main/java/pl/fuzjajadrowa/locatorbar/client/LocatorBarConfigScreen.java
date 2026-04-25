package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarOffset;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LocatorBarConfigScreen extends Screen {
    private static final int TOTAL_PAGES = 3;
    private static final float SCALE_MIN = 0.5F;
    private static final float SCALE_MAX = 2.0F;
    private static final float SCALE_STEP = 0.05F;
    private static final float VIEW_ANGLE_MIN = 30.0F;
    private static final float VIEW_ANGLE_MAX = 180.0F;
    private static final float VIEW_ANGLE_STEP = 5.0F;
    private static final float MARKER_SCALE_MIN = 0.5F;
    private static final float MARKER_SCALE_MAX = 2.0F;
    private static final float MARKER_SCALE_STEP = 0.05F;
    private static final int MAX_PLAYERS_MIN = 1;
    private static final int MAX_PLAYERS_MAX = 64;
    private static final int MAX_WAYPOINTS_MIN = 1;
    private static final int MAX_WAYPOINTS_MAX = 64;

    private final Screen parent;
    private LocatorBarStyle selectedStyle;
    private float selectedScale;
    private LocatorBarOffset selectedOffset;
    private float selectedViewAngle;
    private boolean selectedShowCoordinates;
    private CoordinatesFormat selectedCoordinatesFormat;
    private boolean selectedShowDays;
    private DaysDisplayOrder selectedDaysDisplayOrder;
    private boolean selectedShowWorldDirections;
    private float selectedWorldDirectionsScale;
    private boolean selectedShowPlayerHeads;
    private float selectedPlayerHeadsScale;
    private boolean selectedPlayerHeadOutline;
    private int selectedMaxVisiblePlayers;
    private boolean selectedShowWaypoints;
    private float selectedWaypointsScale;
    private int selectedMaxVisibleWaypoints;
    private int page = 0;

    private ConfigSlider scaleSlider;
    private ConfigSlider viewAngleSlider;
    private Button styleButton;
    private Button offsetButton;
    private Button showCoordinatesButton;
    private Button coordinatesFormatButton;
    private Button showDaysButton;
    private Button daysDisplayOrderButton;

    private Button showWorldDirectionsButton;
    private ConfigSlider worldDirectionsScaleSlider;
    private Button showPlayerHeadsButton;
    private ConfigSlider playerHeadsScaleSlider;
    private Button playerHeadOutlineButton;
    private ConfigSlider maxVisiblePlayersSlider;

    private Button showWaypointsButton;
    private ConfigSlider waypointsScaleSlider;
    private ConfigSlider maxVisibleWaypointsSlider;

    private Button previousPageButton;
    private Button nextPageButton;

    public LocatorBarConfigScreen(Screen parent) {
        super(Component.literal("Locator bar"));
        this.parent = parent;
        this.selectedStyle = LocatorBarConfig.getStyle();
        this.selectedScale = LocatorBarConfig.getScale();
        this.selectedOffset = LocatorBarConfig.getOffset();
        this.selectedViewAngle = LocatorBarConfig.getViewAngle();
        this.selectedShowCoordinates = LocatorBarConfig.isShowCoordinates();
        this.selectedCoordinatesFormat = LocatorBarConfig.getCoordinatesFormat();
        this.selectedShowDays = LocatorBarConfig.isShowDays();
        this.selectedDaysDisplayOrder = LocatorBarConfig.getDaysDisplayOrder();
        this.selectedShowWorldDirections = LocatorBarConfig.isShowWorldDirections();
        this.selectedWorldDirectionsScale = LocatorBarConfig.getWorldDirectionsScale();
        this.selectedShowPlayerHeads = LocatorBarConfig.isShowPlayerHeads();
        this.selectedPlayerHeadsScale = LocatorBarConfig.getPlayerHeadsScale();
        this.selectedPlayerHeadOutline = LocatorBarConfig.isPlayerHeadOutline();
        this.selectedMaxVisiblePlayers = LocatorBarConfig.getMaxVisiblePlayers();
        this.selectedShowWaypoints = LocatorBarConfig.isShowWaypoints();
        this.selectedWaypointsScale = LocatorBarConfig.getWaypointsScale();
        this.selectedMaxVisibleWaypoints = LocatorBarConfig.getMaxVisibleWaypoints();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int controlsY = this.height - 28;
        int controlX = centerX + 20;

        styleButton = Button.builder(styleButtonText(), button -> cycleStyle()).bounds(controlX, 54, 120, 20).build();
        addRenderableWidget(styleButton);

        scaleSlider = new ConfigSlider(controlX, 80, 120, 20, Component.literal("Scale"),
                SCALE_MIN, SCALE_MAX, SCALE_STEP, selectedScale,
                value -> {
                    selectedScale = value;
                    applyAndSave();
                },
                value -> String.format(Locale.ROOT, "%.2fx", value));
        addRenderableWidget(scaleSlider);

        offsetButton = Button.builder(offsetButtonText(), button -> cycleOffset()).bounds(controlX, 106, 120, 20).build();
        addRenderableWidget(offsetButton);

        viewAngleSlider = new ConfigSlider(controlX, 132, 120, 20, Component.literal("View angle"),
                VIEW_ANGLE_MIN, VIEW_ANGLE_MAX, VIEW_ANGLE_STEP, selectedViewAngle,
                value -> {
                    selectedViewAngle = value;
                    applyAndSave();
                },
                value -> Integer.toString(Math.round(value)) + "\u00b0");
        addRenderableWidget(viewAngleSlider);

        showCoordinatesButton = Button.builder(showCoordinatesButtonText(), button -> toggleShowCoordinates())
                .bounds(controlX, 158, 120, 20).build();
        addRenderableWidget(showCoordinatesButton);

        coordinatesFormatButton = Button.builder(coordinatesFormatButtonText(), button -> cycleCoordinatesFormat())
                .bounds(controlX, 184, 120, 20).build();
        addRenderableWidget(coordinatesFormatButton);

        showDaysButton = Button.builder(showDaysButtonText(), button -> toggleShowDays())
                .bounds(controlX, 210, 120, 20).build();
        addRenderableWidget(showDaysButton);

        daysDisplayOrderButton = Button.builder(daysDisplayOrderButtonText(), button -> cycleDaysDisplayOrder())
                .bounds(controlX, 236, 120, 20).build();
        addRenderableWidget(daysDisplayOrderButton);

        showWorldDirectionsButton = Button.builder(showWorldDirectionsButtonText(), button -> toggleShowWorldDirections())
                .bounds(controlX, 54, 120, 20).build();
        addRenderableWidget(showWorldDirectionsButton);

        worldDirectionsScaleSlider = new ConfigSlider(controlX, 80, 120, 20, Component.literal("Directions size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedWorldDirectionsScale,
                value -> {
                    selectedWorldDirectionsScale = value;
                    applyAndSave();
                },
                value -> String.format(Locale.ROOT, "%.2fx", value));
        addRenderableWidget(worldDirectionsScaleSlider);

        showPlayerHeadsButton = Button.builder(showPlayerHeadsButtonText(), button -> toggleShowPlayerHeads())
                .bounds(controlX, 106, 120, 20).build();
        addRenderableWidget(showPlayerHeadsButton);

        playerHeadsScaleSlider = new ConfigSlider(controlX, 132, 120, 20, Component.literal("Heads size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedPlayerHeadsScale,
                value -> {
                    selectedPlayerHeadsScale = value;
                    applyAndSave();
                },
                value -> String.format(Locale.ROOT, "%.2fx", value));
        addRenderableWidget(playerHeadsScaleSlider);

        playerHeadOutlineButton = Button.builder(playerHeadOutlineButtonText(), button -> togglePlayerHeadOutline())
                .bounds(controlX, 158, 120, 20).build();
        addRenderableWidget(playerHeadOutlineButton);

        maxVisiblePlayersSlider = new ConfigSlider(controlX, 184, 120, 20, Component.literal("Max players"),
                MAX_PLAYERS_MIN, MAX_PLAYERS_MAX, 1.0F, selectedMaxVisiblePlayers,
                value -> {
                    selectedMaxVisiblePlayers = Math.round(value);
                    applyAndSave();
                },
                value -> Integer.toString(Math.round(value)));
        addRenderableWidget(maxVisiblePlayersSlider);

        showWaypointsButton = Button.builder(showWaypointsButtonText(), button -> toggleShowWaypoints())
                .bounds(controlX, 54, 120, 20).build();
        addRenderableWidget(showWaypointsButton);

        waypointsScaleSlider = new ConfigSlider(controlX, 80, 120, 20, Component.literal("Waypoints size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedWaypointsScale,
                value -> {
                    selectedWaypointsScale = value;
                    applyAndSave();
                },
                value -> String.format(Locale.ROOT, "%.2fx", value));
        addRenderableWidget(waypointsScaleSlider);

        maxVisibleWaypointsSlider = new ConfigSlider(controlX, 106, 120, 20, Component.literal("Max waypoints"),
                MAX_WAYPOINTS_MIN, MAX_WAYPOINTS_MAX, 1.0F, selectedMaxVisibleWaypoints,
                value -> {
                    selectedMaxVisibleWaypoints = Math.round(value);
                    applyAndSave();
                },
                value -> Integer.toString(Math.round(value)));
        addRenderableWidget(maxVisibleWaypointsSlider);

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(centerX - 35, controlsY, 70, 20).build());

        previousPageButton = Button.builder(Component.literal("<"), button -> previousPage())
                .bounds(centerX + 80, controlsY, 30, 20).build();
        addRenderableWidget(previousPageButton);

        nextPageButton = Button.builder(Component.literal(">"), button -> nextPage())
                .bounds(centerX + 116, controlsY, 30, 20).build();
        addRenderableWidget(nextPageButton);

        updatePageState();
        updateControlStates();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int leftLabelX = centerX - 138;
        String pageTitle = switch (page) {
            case 0 -> "1. General";
            case 1 -> "2. Markers";
            default -> "3. Waypoints";
        };

        guiGraphics.drawCenteredString(this.font, Component.literal("Locator bar config"), centerX, 14, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.literal(pageTitle), centerX, 34, 0xFFFFFF);

        if (page == 0) {
            guiGraphics.drawString(this.font, Component.literal("Locator bar style"), leftLabelX, 60, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Locator bar scale"), leftLabelX, 86, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Locator bar offset"), leftLabelX, 112, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("View angle"), leftLabelX, 138, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Show coordinates"), leftLabelX, 164, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Coordinates format"), leftLabelX, 190, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Show days"), leftLabelX, 216, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Days display order"), leftLabelX, 242, 0xFFFFFF, false);
        } else if (page == 1) {
            guiGraphics.drawString(this.font, Component.literal("Show world directions"), leftLabelX, 60, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Directions size"), leftLabelX, 86, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Show player heads"), leftLabelX, 112, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Heads size"), leftLabelX, 138, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Head outline"), leftLabelX, 164, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Max visible players"), leftLabelX, 190, 0xFFFFFF, false);
        } else {
            guiGraphics.drawString(this.font, Component.literal("Show waypoints"), leftLabelX, 60, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Waypoints size"), leftLabelX, 86, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.literal("Max visible waypoints"), leftLabelX, 112, 0xFFFFFF, false);
        }

        guiGraphics.drawString(this.font, Component.literal("Page " + (page + 1) + "/" + TOTAL_PAGES), centerX + 91, this.height - 40, 0xA0A0A0, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void cycleStyle() {
        selectedStyle = selectedStyle.next();
        styleButton.setMessage(styleButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component styleButtonText() {
        return Component.literal(selectedStyle.label());
    }

    private void cycleOffset() {
        selectedOffset = selectedOffset.next();
        offsetButton.setMessage(offsetButtonText());
        applyAndSave();
    }

    private Component offsetButtonText() {
        return Component.literal(selectedOffset.label());
    }

    private void toggleShowCoordinates() {
        selectedShowCoordinates = !selectedShowCoordinates;
        showCoordinatesButton.setMessage(showCoordinatesButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showCoordinatesButtonText() {
        return Component.literal(selectedShowCoordinates ? "On" : "Off");
    }

    private void cycleCoordinatesFormat() {
        selectedCoordinatesFormat = selectedCoordinatesFormat.next();
        coordinatesFormatButton.setMessage(coordinatesFormatButtonText());
        applyAndSave();
    }

    private Component coordinatesFormatButtonText() {
        return Component.literal(selectedCoordinatesFormat.label());
    }

    private void toggleShowDays() {
        selectedShowDays = !selectedShowDays;
        showDaysButton.setMessage(showDaysButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showDaysButtonText() {
        return Component.literal(selectedShowDays ? "On" : "Off");
    }

    private void cycleDaysDisplayOrder() {
        selectedDaysDisplayOrder = selectedDaysDisplayOrder.next();
        daysDisplayOrderButton.setMessage(daysDisplayOrderButtonText());
        applyAndSave();
    }

    private Component daysDisplayOrderButtonText() {
        return Component.literal(selectedDaysDisplayOrder.label());
    }

    private void toggleShowWorldDirections() {
        selectedShowWorldDirections = !selectedShowWorldDirections;
        showWorldDirectionsButton.setMessage(showWorldDirectionsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showWorldDirectionsButtonText() {
        return Component.literal(selectedShowWorldDirections ? "On" : "Off");
    }

    private void toggleShowPlayerHeads() {
        selectedShowPlayerHeads = !selectedShowPlayerHeads;
        showPlayerHeadsButton.setMessage(showPlayerHeadsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showPlayerHeadsButtonText() {
        return Component.literal(selectedShowPlayerHeads ? "On" : "Off");
    }

    private void togglePlayerHeadOutline() {
        selectedPlayerHeadOutline = !selectedPlayerHeadOutline;
        playerHeadOutlineButton.setMessage(playerHeadOutlineButtonText());
        applyAndSave();
    }

    private Component playerHeadOutlineButtonText() {
        return Component.literal(selectedPlayerHeadOutline ? "On" : "Off");
    }

    private void toggleShowWaypoints() {
        selectedShowWaypoints = !selectedShowWaypoints;
        showWaypointsButton.setMessage(showWaypointsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showWaypointsButtonText() {
        return Component.literal(selectedShowWaypoints ? "On" : "Off");
    }

    private void updateControlStates() {
        boolean styleEnabled = selectedStyle != LocatorBarStyle.OFF;
        boolean classicStyle = selectedStyle == LocatorBarStyle.CLASSIC;
        boolean canChangeCoordinatesFormat = styleEnabled && !classicStyle && selectedShowCoordinates;
        boolean canChangeDaysOrder = styleEnabled && !classicStyle && selectedShowCoordinates && selectedShowDays;
        boolean canChangeDirectionScale = styleEnabled && selectedShowWorldDirections;
        boolean canChangeHeadSettings = styleEnabled && selectedShowPlayerHeads;
        boolean canChangeWaypoints = styleEnabled && selectedShowWaypoints;

        scaleSlider.active = styleEnabled && !classicStyle;
        offsetButton.active = styleEnabled && !classicStyle;
        viewAngleSlider.active = styleEnabled && !classicStyle;
        showCoordinatesButton.active = styleEnabled && !classicStyle;
        coordinatesFormatButton.active = canChangeCoordinatesFormat;
        showDaysButton.active = styleEnabled && !classicStyle;
        daysDisplayOrderButton.active = canChangeDaysOrder;

        showWorldDirectionsButton.active = styleEnabled;
        worldDirectionsScaleSlider.active = canChangeDirectionScale;
        showPlayerHeadsButton.active = styleEnabled;
        playerHeadsScaleSlider.active = canChangeHeadSettings;
        playerHeadOutlineButton.active = canChangeHeadSettings;
        maxVisiblePlayersSlider.active = canChangeHeadSettings;

        showWaypointsButton.active = styleEnabled;
        waypointsScaleSlider.active = canChangeWaypoints;
        maxVisibleWaypointsSlider.active = canChangeWaypoints;
    }

    private void updatePageState() {
        boolean firstPage = page == 0;
        boolean secondPage = page == 1;
        boolean thirdPage = page == 2;

        styleButton.visible = firstPage;
        scaleSlider.visible = firstPage;
        offsetButton.visible = firstPage;
        viewAngleSlider.visible = firstPage;
        showCoordinatesButton.visible = firstPage;
        coordinatesFormatButton.visible = firstPage;
        showDaysButton.visible = firstPage;
        daysDisplayOrderButton.visible = firstPage;

        showWorldDirectionsButton.visible = secondPage;
        worldDirectionsScaleSlider.visible = secondPage;
        showPlayerHeadsButton.visible = secondPage;
        playerHeadsScaleSlider.visible = secondPage;
        playerHeadOutlineButton.visible = secondPage;
        maxVisiblePlayersSlider.visible = secondPage;

        showWaypointsButton.visible = thirdPage;
        waypointsScaleSlider.visible = thirdPage;
        maxVisibleWaypointsSlider.visible = thirdPage;

        previousPageButton.active = page > 0;
        nextPageButton.active = page < TOTAL_PAGES - 1;
    }

    private void applyAndSave() {
        LocatorBarConfig.setStyle(selectedStyle);
        LocatorBarConfig.setScale(selectedScale);
        LocatorBarConfig.setOffset(selectedOffset);
        LocatorBarConfig.setViewAngle(selectedViewAngle);
        LocatorBarConfig.setShowCoordinates(selectedShowCoordinates);
        LocatorBarConfig.setCoordinatesFormat(selectedCoordinatesFormat);
        LocatorBarConfig.setShowDays(selectedShowDays);
        LocatorBarConfig.setDaysDisplayOrder(selectedDaysDisplayOrder);
        LocatorBarConfig.setShowWorldDirections(selectedShowWorldDirections);
        LocatorBarConfig.setWorldDirectionsScale(selectedWorldDirectionsScale);
        LocatorBarConfig.setShowPlayerHeads(selectedShowPlayerHeads);
        LocatorBarConfig.setPlayerHeadsScale(selectedPlayerHeadsScale);
        LocatorBarConfig.setPlayerHeadOutline(selectedPlayerHeadOutline);
        LocatorBarConfig.setMaxVisiblePlayers(selectedMaxVisiblePlayers);
        LocatorBarConfig.setShowWaypoints(selectedShowWaypoints);
        LocatorBarConfig.setWaypointsScale(selectedWaypointsScale);
        LocatorBarConfig.setMaxVisibleWaypoints(selectedMaxVisibleWaypoints);
        LocatorBarConfig.save();
    }

    private void previousPage() {
        page = Math.max(0, page - 1);
        updatePageState();
    }

    private void nextPage() {
        page = Math.min(TOTAL_PAGES - 1, page + 1);
        updatePageState();
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