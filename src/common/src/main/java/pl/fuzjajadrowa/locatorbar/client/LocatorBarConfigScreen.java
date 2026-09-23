package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;

import java.util.List;
import java.util.Locale;

import pl.fuzjajadrowa.locatorbar.client.WaypointConfigPage.ManagedWaypoint;

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
    private final ConfigScreenState state = new ConfigScreenState();
    private int page = 0;

    private ConfigList list;

    private ConfigSlider scaleSlider;
    private ConfigSlider customOffsetXSlider;
    private ConfigSlider customOffsetYSlider;
    private ConfigSlider viewAngleSlider;
    private Button styleButton;
    private Button showCoordinatesButton;
    private Button elementsOnXpBarButton;
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
    private Button showDeathWaypointButton;
    private ConfigSlider waypointsScaleSlider;
    private ConfigSlider maxVisibleWaypointsSlider;

    private Button previousPageButton;
    private Button nextPageButton;

    public LocatorBarConfigScreen(Screen parent) {
        super(Component.translatable("locatorbar.config.title"));
        this.parent = parent;
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
        this.list = new ConfigList(this.minecraft, this.width, listHeight, 50, 25, this.height, this::updatePageState);
        this.addRenderableWidget(this.list);

        styleButton = Button.builder(styleButtonText(), button -> cycleStyle()).bounds(0, 0, 120, 20).build();

        scaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.scale"),
                SCALE_MIN, SCALE_MAX, SCALE_STEP, state.selectedScale,
                value -> { state.selectedScale = value; applyRuntime(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        customOffsetXSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.custom_offset_x"),
                -500.0F, 500.0F, 1.0F, (float) state.selectedCustomOffsetX,
                value -> { state.selectedCustomOffsetX = Math.round(value); applyRuntime(); },
                value -> {
                    int val = Math.round(value);
                    return val >= 0 ? "+" + val + "px" : val + "px";
                });

        customOffsetYSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.custom_offset_y"),
                -500.0F, 500.0F, 1.0F, (float) state.selectedCustomOffsetY,
                value -> { state.selectedCustomOffsetY = Math.round(value); applyRuntime(); },
                value -> {
                    int val = Math.round(value);
                    return val >= 0 ? "+" + val + "px" : val + "px";
                });

        viewAngleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.view_angle"),
                VIEW_ANGLE_MIN, VIEW_ANGLE_MAX, VIEW_ANGLE_STEP, state.selectedViewAngle,
                value -> { state.selectedViewAngle = value; applyRuntime(); },
                value -> Integer.toString(Math.round(value)) + "\u00b0");

        showCoordinatesButton = Button.builder(showCoordinatesButtonText(), button -> toggleShowCoordinates()).bounds(0, 0, 120, 20).build();
        elementsOnXpBarButton = Button.builder(elementsOnXpBarButtonText(), button -> toggleElementsOnXpBar()).bounds(0, 0, 120, 20).build();
        coordinatesFormatButton = Button.builder(coordinatesFormatButtonText(), button -> cycleCoordinatesFormat()).bounds(0, 0, 120, 20).build();
        showDaysButton = Button.builder(showDaysButtonText(), button -> toggleShowDays()).bounds(0, 0, 120, 20).build();
        daysDisplayOrderButton = Button.builder(daysDisplayOrderButtonText(), button -> cycleDaysDisplayOrder()).bounds(0, 0, 120, 20).build();

        showWorldDirectionsButton = Button.builder(showWorldDirectionsButtonText(), button -> toggleShowWorldDirections()).bounds(0, 0, 120, 20).build();

        worldDirectionsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.directions_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, state.selectedWorldDirectionsScale,
                value -> { state.selectedWorldDirectionsScale = value; applyRuntime(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        showPlayerHeadsButton = Button.builder(playerMarkerTypeButtonText(), button -> cyclePlayerMarkerType()).bounds(0, 0, 120, 20).build();

        playerHeadsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.heads_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, state.selectedPlayerMarkersScale,
                value -> { state.selectedPlayerMarkersScale = value; applyRuntime(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        playerHeadOutlineButton = Button.builder(playerMarkerOutlineButtonText(), button -> togglePlayerMarkerOutline()).bounds(0, 0, 120, 20).build();

        maxVisiblePlayersSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.max_players"),
                MAX_PLAYERS_MIN, MAX_PLAYERS_MAX, 1.0F, (float) state.selectedMaxVisiblePlayers,
                value -> { state.selectedMaxVisiblePlayers = Math.round(value); applyRuntime(); },
                value -> Integer.toString(Math.round(value)));

        showWaypointsButton = Button.builder(showWaypointsButtonText(), button -> toggleShowWaypoints()).bounds(0, 0, 120, 20).build();
        showDeathWaypointButton = Button.builder(showDeathWaypointButtonText(), button -> toggleShowDeathWaypoint()).bounds(0, 0, 120, 20).build();

        waypointsScaleSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.waypoints_size"),
                MARKER_SCALE_MIN, MARKER_SCALE_MAX, MARKER_SCALE_STEP, state.selectedWaypointsScale,
                value -> { state.selectedWaypointsScale = value; applyRuntime(); },
                value -> String.format(Locale.ROOT, "%.2fx", value));

        maxVisibleWaypointsSlider = new ConfigSlider(0, 0, 120, 20, Component.translatable("locatorbar.config.field.max_waypoints"),
                MAX_WAYPOINTS_MIN, MAX_WAYPOINTS_MAX, 1.0F, state.selectedMaxVisibleWaypoints,
                value -> { state.selectedMaxVisibleWaypoints = Math.round(value); applyRuntime(); },
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

    //? if >=26.1 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderHeader(guiGraphics);
    }
    //?} elif >=1.21.11 {
    /*@Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderHeader(guiGraphics);
    }
    *///?} else {
    /*@Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21 {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        //?} else {
        renderBackground(guiGraphics);
        //?}

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderHeader(guiGraphics);
    }
    *///?}

    private void renderHeader(GuiGraphicsExtractor guiGraphics) {
        int centerX = this.width / 2;
        Component pageTitle = switch (page) {
            case 0 -> Component.translatable("locatorbar.config.page.general");
            case 1 -> Component.translatable("locatorbar.config.page.markers");
            default -> Component.translatable("locatorbar.config.page.waypoints");
        };

        guiGraphics.centeredText(this.font, Component.translatable("locatorbar.config.header"), centerX, 14, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, pageTitle, centerX, 30, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            //? if >=26.2 {
            this.minecraft.setScreenAndShow(parent);
            //?} else {
            /*this.minecraft.setScreen(parent);
            *///?}
        }
    }

    private void cycleStyle() {
        state.selectedStyle = state.selectedStyle.next();
        styleButton.setMessage(styleButtonText());
        applyRuntime();
        updatePageState();
        updateControlStates();
    }

    private Component styleButtonText() {
        return Component.translatable(state.selectedStyle.translationKey());
    }

    private void toggleShowCoordinates() {
        state.selectedShowCoordinates = !state.selectedShowCoordinates;
        showCoordinatesButton.setMessage(showCoordinatesButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component showCoordinatesButtonText() {
        return Component.translatable(state.selectedShowCoordinates ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void toggleElementsOnXpBar() {
        state.selectedElementsOnXpBar = !state.selectedElementsOnXpBar;
        elementsOnXpBarButton.setMessage(elementsOnXpBarButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component elementsOnXpBarButtonText() {
        return Component.translatable(state.selectedElementsOnXpBar ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void cycleCoordinatesFormat() {
        state.selectedCoordinatesFormat = state.selectedCoordinatesFormat.next();
        coordinatesFormatButton.setMessage(coordinatesFormatButtonText());
        applyRuntime();
    }

    private Component coordinatesFormatButtonText() {
        return Component.translatable(state.selectedCoordinatesFormat.translationKey());
    }

    private void toggleShowDays() {
        state.selectedShowDays = !state.selectedShowDays;
        showDaysButton.setMessage(showDaysButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component showDaysButtonText() {
        return Component.translatable(state.selectedShowDays ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void cycleDaysDisplayOrder() {
        state.selectedDaysDisplayOrder = state.selectedDaysDisplayOrder.next();
        daysDisplayOrderButton.setMessage(daysDisplayOrderButtonText());
        applyRuntime();
    }

    private Component daysDisplayOrderButtonText() {
        return Component.translatable(state.selectedDaysDisplayOrder.translationKey());
    }

    private void toggleShowWorldDirections() {
        state.selectedShowWorldDirections = !state.selectedShowWorldDirections;
        showWorldDirectionsButton.setMessage(showWorldDirectionsButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component showWorldDirectionsButtonText() {
        return Component.translatable(state.selectedShowWorldDirections ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void cyclePlayerMarkerType() {
        if (LocatorBarConfig.hasServerSettings()) {
            var server = LocatorBarConfig.getServerSettings();
            if (server != null && server.playerMarkerType() == PlayerMarkerType.DOTS) {
                state.selectedPlayerMarkerType = state.selectedPlayerMarkerType == PlayerMarkerType.DOTS ? PlayerMarkerType.OFF : PlayerMarkerType.DOTS;
            } else {
                state.selectedPlayerMarkerType = state.selectedPlayerMarkerType.next();
            }
        } else {
            state.selectedPlayerMarkerType = state.selectedPlayerMarkerType.next();
        }
        showPlayerHeadsButton.setMessage(playerMarkerTypeButtonText());
        applyRuntime();
        updateControlStates();
        updatePageState();
    }

    private Component playerMarkerTypeButtonText() {
        return Component.translatable(state.selectedPlayerMarkerType.translationKey());
    }

    private void togglePlayerMarkerOutline() {
        state.selectedPlayerMarkerOutline = !state.selectedPlayerMarkerOutline;
        playerHeadOutlineButton.setMessage(playerMarkerOutlineButtonText());
        applyRuntime();
    }

    private Component playerMarkerOutlineButtonText() {
        return Component.translatable(state.selectedPlayerMarkerOutline ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void toggleShowWaypoints() {
        state.selectedShowWaypoints = !state.selectedShowWaypoints;
        showWaypointsButton.setMessage(showWaypointsButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component showWaypointsButtonText() {
        return Component.translatable(state.selectedShowWaypoints ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void toggleShowDeathWaypoint() {
        state.selectedShowDeathWaypoint = !state.selectedShowDeathWaypoint;
        showDeathWaypointButton.setMessage(showDeathWaypointButtonText());
        applyRuntime();
        updateControlStates();
    }

    private Component showDeathWaypointButtonText() {
        return Component.translatable(state.selectedShowDeathWaypoint ? "locatorbar.option.on" : "locatorbar.option.off");
    }

    private void updateControlStates() {
        boolean styleEnabled = state.selectedStyle != LocatorBarStyle.OFF;
        boolean classicStyle = state.selectedStyle == LocatorBarStyle.CLASSIC;
        boolean reworkedStyle = state.selectedStyle == LocatorBarStyle.REWORKED;
        boolean canChangeCoordinatesFormat = styleEnabled && !classicStyle && state.selectedShowCoordinates;
        boolean canChangeDaysOrder = styleEnabled && !classicStyle && state.selectedShowCoordinates && state.selectedShowDays;
        boolean canChangeDirectionScale = styleEnabled && state.selectedShowWorldDirections;
        boolean canChangeMarkerSettings = styleEnabled && state.selectedPlayerMarkerType != PlayerMarkerType.OFF;
        boolean canChangeOutline = canChangeMarkerSettings && state.selectedPlayerMarkerType == PlayerMarkerType.HEADS;
        boolean canChangeWaypoints = styleEnabled && state.selectedShowWaypoints;

        var server = LocatorBarConfig.getServerSettings();
        boolean hasServer = LocatorBarConfig.hasServerSettings() && server != null;

        styleButton.active = !hasServer;
        scaleSlider.active = reworkedStyle;
        viewAngleSlider.active = reworkedStyle;
        showCoordinatesButton.active = reworkedStyle && !hasServer;
        elementsOnXpBarButton.active = classicStyle;
        coordinatesFormatButton.active = canChangeCoordinatesFormat;
        showDaysButton.active = reworkedStyle && !hasServer;
        daysDisplayOrderButton.active = canChangeDaysOrder;

        showWorldDirectionsButton.active = styleEnabled && !hasServer;
        worldDirectionsScaleSlider.active = canChangeDirectionScale;
        showPlayerHeadsButton.active = styleEnabled && !hasServer;
        playerHeadsScaleSlider.active = canChangeMarkerSettings;
        playerHeadOutlineButton.active = canChangeOutline;
        maxVisiblePlayersSlider.active = canChangeMarkerSettings && !hasServer;

        showWaypointsButton.active = styleEnabled && !hasServer;
        showDeathWaypointButton.active = styleEnabled && state.selectedShowWaypoints && !hasServer;
        waypointsScaleSlider.active = canChangeWaypoints;
        maxVisibleWaypointsSlider.active = canChangeWaypoints && !hasServer;
    }

    private void updatePageState() {
        if (this.list != null) {
            this.list.clearList();

            if (page == 0) {
                this.list.addEntry(Component.translatable("locatorbar.config.field.style"), styleButton);
                if (state.selectedStyle == LocatorBarStyle.REWORKED) {
                    this.list.addEntry(Component.translatable("locatorbar.config.field.scale"), scaleSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.custom_offset_x"), customOffsetXSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.custom_offset_y"), customOffsetYSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.view_angle"), viewAngleSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.show_coordinates"), showCoordinatesButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.coordinates_format"), coordinatesFormatButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.show_days"), showDaysButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.days_display_order"), daysDisplayOrderButton);
                } else if (state.selectedStyle == LocatorBarStyle.CLASSIC) {
                    this.list.addEntry(Component.translatable("locatorbar.config.field.elements_on_xp_bar"), elementsOnXpBarButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.custom_offset_x"), customOffsetXSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.custom_offset_y"), customOffsetYSlider);
                }
            } else if (page == 1) {
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_world_directions"), showWorldDirectionsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.directions_size"), worldDirectionsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_player_heads"), showPlayerHeadsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.heads_size"), playerHeadsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.head_outline"), playerHeadOutlineButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.max_visible_players"), maxVisiblePlayersSlider);
            } else {
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_waypoints"), showWaypointsButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.show_death_waypoint"), showDeathWaypointButton);
                this.list.addEntry(Component.translatable("locatorbar.config.field.waypoints_size"), waypointsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.max_visible_waypoints"), maxVisibleWaypointsSlider);

                if (state.selectedStyle != LocatorBarStyle.OFF && state.selectedShowWaypoints) {
                    List<ManagedWaypoint> waypoints = WaypointConfigPage.collectManagedWaypoints();
                    if (!waypoints.isEmpty()) {
                        this.list.addHeaderEntry(Component.translatable("locatorbar.config.header.waypoint_manager"));
                        for (ManagedWaypoint waypoint : waypoints) {
                            this.list.addWaypointEntry(waypoint);
                        }
                    }
                }
            }
        }

        previousPageButton.active = page > 0;
        nextPageButton.active = page < TOTAL_PAGES - 1;
    }

    private boolean dirty;

    private void applyRuntime() {
        state.apply();
        dirty = true;
    }

    @Override
    public void removed() {
        if (dirty) {
            LocatorBarConfig.save();
            dirty = false;
        }
        super.removed();
    }

    private void previousPage() {
        page = Math.max(0, page - 1);
        updatePageState();
    }

    private void nextPage() {
        page = Math.min(TOTAL_PAGES - 1, page + 1);
        updatePageState();
    }

}
