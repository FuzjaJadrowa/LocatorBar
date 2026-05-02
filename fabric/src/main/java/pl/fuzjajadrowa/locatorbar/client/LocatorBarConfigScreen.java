package pl.fuzjajadrowa.locatorbar.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarOffset;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

import java.util.List;
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
    private static final int DONE_BUTTON_WIDTH = 90;
    private static final int FOOTER_SECTION_GAP = 28;
    private static final int PAGE_BUTTON_WIDTH = 30;
    private static final int PAGE_BUTTON_GAP = 6;
    private static final int PAGE_NAV_WIDTH = (PAGE_BUTTON_WIDTH * 2) + PAGE_BUTTON_GAP;

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

    private ConfigList list;

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
        super(Component.translatable("locatorbar.config.title"));
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
        int footerTotalWidth = DONE_BUTTON_WIDTH + FOOTER_SECTION_GAP + PAGE_NAV_WIDTH;
        int footerStartX = centerX - (footerTotalWidth / 2);
        int doneX = footerStartX;
        int pageNavX = doneX + DONE_BUTTON_WIDTH + FOOTER_SECTION_GAP;

        int listHeight = this.height - 90;
        this.list = new ConfigList(this.minecraft, this.width, listHeight, 50, 25);
        this.addRenderableWidget(this.list);

        styleButton = Button.builder(styleButtonText(), button -> cycleStyle()).bounds(0, 0, 120, 20).build();

        scaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.scale"),
                SCALE_MIN, SCALE_MAX, SCALE_STEP, selectedScale,
                value -> { selectedScale = value; applyAndSave(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        offsetButton = Button.builder(offsetButtonText(), button -> cycleOffset()).bounds(0, 0, 120, 20).build();

        viewAngleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.view_angle"),
                VIEW_ANGLE_MIN, VIEW_ANGLE_MAX, VIEW_ANGLE_STEP, selectedViewAngle,
                value -> { selectedViewAngle = value; applyAndSave(); },
                value -> Math.round(value) + "\u00b0");

        showCoordinatesButton = Button.builder(showCoordinatesButtonText(), button -> toggleShowCoordinates()).bounds(0, 0, 120, 20).build();
        coordinatesFormatButton = Button.builder(coordinatesFormatButtonText(), button -> cycleCoordinatesFormat()).bounds(0, 0, 120, 20).build();
        showDaysButton = Button.builder(showDaysButtonText(), button -> toggleShowDays()).bounds(0, 0, 120, 20).build();
        daysDisplayOrderButton = Button.builder(daysDisplayOrderButtonText(), button -> cycleDaysDisplayOrder()).bounds(0, 0, 120, 20).build();

        showWorldDirectionsButton = Button.builder(showWorldDirectionsButtonText(), button -> toggleShowWorldDirections()).bounds(0, 0, 120, 20).build();

        worldDirectionsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.directions_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedWorldDirectionsScale,
                value -> { selectedWorldDirectionsScale = value; applyAndSave(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        showPlayerHeadsButton = Button.builder(showPlayerHeadsButtonText(), button -> toggleShowPlayerHeads()).bounds(0, 0, 120, 20).build();

        playerHeadsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.heads_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedPlayerHeadsScale,
                value -> { selectedPlayerHeadsScale = value; applyAndSave(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        playerHeadOutlineButton = Button.builder(playerHeadOutlineButtonText(), button -> togglePlayerHeadOutline()).bounds(0, 0, 120, 20).build();

        maxVisiblePlayersSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.max_players"),
                MAX_PLAYERS_MIN, MAX_PLAYERS_MAX, 1.0F, selectedMaxVisiblePlayers,
                value -> { selectedMaxVisiblePlayers = Math.round(value); applyAndSave(); },
                value -> Integer.toString(Math.round(value)));

        showWaypointsButton = Button.builder(showWaypointsButtonText(), button -> toggleShowWaypoints()).bounds(0, 0, 120, 20).build();

        waypointsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.waypoints_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, selectedWaypointsScale,
                value -> { selectedWaypointsScale = value; applyAndSave(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        maxVisibleWaypointsSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.max_waypoints"),
                MAX_WAYPOINTS_MIN, MAX_WAYPOINTS_MAX, 1.0F, selectedMaxVisibleWaypoints,
                value -> { selectedMaxVisibleWaypoints = Math.round(value); applyAndSave(); },
                value -> Integer.toString(Math.round(value)));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(doneX, controlsY, DONE_BUTTON_WIDTH, 20).build());

        previousPageButton = Button.builder(Component.literal("<"), button -> previousPage())
                .bounds(pageNavX, controlsY, PAGE_BUTTON_WIDTH, 20).build();
        addRenderableWidget(previousPageButton);

        nextPageButton = Button.builder(Component.literal(">"), button -> nextPage())
                .bounds(pageNavX + PAGE_BUTTON_WIDTH + PAGE_BUTTON_GAP, controlsY, PAGE_BUTTON_WIDTH, 20).build();
        addRenderableWidget(nextPageButton);

        updatePageState();
        updateControlStates();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        Component pageTitle = switch (page) {
            case 0 -> Component.translatable("locatorbar.config.page.general");
            case 1 -> Component.translatable("locatorbar.config.page.markers");
            default -> Component.translatable("locatorbar.config.page.waypoints");
        };

        guiGraphics.drawCenteredString(this.font, Component.translatable("locatorbar.config.header"), centerX, 14, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, pageTitle, centerX, 30, 0xFFFFFFFF);
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
        return Component.translatable(selectedStyle.translationKey());
    }

    private void cycleOffset() {
        selectedOffset = selectedOffset.next();
        offsetButton.setMessage(offsetButtonText());
        applyAndSave();
    }

    private Component offsetButtonText() {
        return Component.translatable(selectedOffset.translationKey());
    }

    private void toggleShowCoordinates() {
        selectedShowCoordinates = !selectedShowCoordinates;
        showCoordinatesButton.setMessage(showCoordinatesButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showCoordinatesButtonText() {
        return Component.translatable(selectedShowCoordinates ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void cycleCoordinatesFormat() {
        selectedCoordinatesFormat = selectedCoordinatesFormat.next();
        coordinatesFormatButton.setMessage(coordinatesFormatButtonText());
        applyAndSave();
    }

    private Component coordinatesFormatButtonText() {
        return Component.translatable(selectedCoordinatesFormat.translationKey());
    }

    private void toggleShowDays() {
        selectedShowDays = !selectedShowDays;
        showDaysButton.setMessage(showDaysButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showDaysButtonText() {
        return Component.translatable(selectedShowDays ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void cycleDaysDisplayOrder() {
        selectedDaysDisplayOrder = selectedDaysDisplayOrder.next();
        daysDisplayOrderButton.setMessage(daysDisplayOrderButtonText());
        applyAndSave();
    }

    private Component daysDisplayOrderButtonText() {
        return Component.translatable(selectedDaysDisplayOrder.translationKey());
    }

    private void toggleShowWorldDirections() {
        selectedShowWorldDirections = !selectedShowWorldDirections;
        showWorldDirectionsButton.setMessage(showWorldDirectionsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showWorldDirectionsButtonText() {
        return Component.translatable(selectedShowWorldDirections ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void toggleShowPlayerHeads() {
        selectedShowPlayerHeads = !selectedShowPlayerHeads;
        showPlayerHeadsButton.setMessage(showPlayerHeadsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showPlayerHeadsButtonText() {
        return Component.translatable(selectedShowPlayerHeads ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void togglePlayerHeadOutline() {
        selectedPlayerHeadOutline = !selectedPlayerHeadOutline;
        playerHeadOutlineButton.setMessage(playerHeadOutlineButtonText());
        applyAndSave();
    }

    private Component playerHeadOutlineButtonText() {
        return Component.translatable(selectedPlayerHeadOutline ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void toggleShowWaypoints() {
        selectedShowWaypoints = !selectedShowWaypoints;
        showWaypointsButton.setMessage(showWaypointsButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component showWaypointsButtonText() {
        return Component.translatable(selectedShowWaypoints ? "locatorbar.option.on" : "locatorbar.option.off");
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
        if (this.list != null) {
            this.list.clearList();

            if (page == 0) {
                this.list.addEntry(Component.translatable("locatorbar.config.field.style"), styleButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.scale"), scaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.offset"), offsetButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.view_angle"), viewAngleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_coordinates"), showCoordinatesButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.coordinates_format"), coordinatesFormatButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_days"), showDaysButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.days_display_order"), daysDisplayOrderButton);
            } else if (page == 1) {
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_world_directions"), showWorldDirectionsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.directions_size"), worldDirectionsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_player_heads"), showPlayerHeadsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.heads_size"), playerHeadsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.head_outline"), playerHeadOutlineButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.max_visible_players"), maxVisiblePlayersSlider);
            } else {
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_waypoints"), showWaypointsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.waypoints_size"), waypointsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.max_visible_waypoints"), maxVisibleWaypointsSlider);
            }
        }

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

    private final class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
        public ConfigList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void clearList() {
            this.clearEntries();
            this.setScrollAmount(0);
        }

        public void addEntry(Component label, AbstractWidget widget) {
            super.addEntry(new Entry(label, widget));
        }

        @Override
        public int getRowWidth() {
            return 340;
        }

        private int getScrollbarPosition() {
            return this.width / 2 + 160;
        }

        class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            private final Component label;
            private final AbstractWidget widget;
            private final List<AbstractWidget> children;

            public Entry(Component label, AbstractWidget widget) {
                this.label = label;
                this.widget = widget;
                this.children = ImmutableList.of(widget);
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int top = this.getContentY();
                int height = this.getContentHeight();
                int centerY = top + (height - LocatorBarConfigScreen.this.font.lineHeight) / 2;

                guiGraphics.drawString(LocatorBarConfigScreen.this.font, label, LocatorBarConfigScreen.this.width / 2 - 138, centerY, 0xFFFFFFFF, false);

                widget.setX(LocatorBarConfigScreen.this.width / 2 + 20);
                widget.setY(top);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }
        }
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
}