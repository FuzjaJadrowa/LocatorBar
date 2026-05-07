package pl.fuzjajadrowa.locatorbar.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig.WaypointConfig;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LocatorBarConfigScreen extends Screen {
    private static final Identifier WAYPOINT_TEXTURE = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/waypoint.png"
    );
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
    private boolean selectedElementsOnXpBar;
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
        this.selectedElementsOnXpBar = LocatorBarConfig.isElementsOnXpBar();
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
                value -> Integer.toString(Math.round(value)) + "\u00b0");

        showCoordinatesButton = Button.builder(showCoordinatesButtonText(), button -> toggleShowCoordinates()).bounds(0, 0, 120, 20).build();
        elementsOnXpBarButton = Button.builder(elementsOnXpBarButtonText(), button -> toggleElementsOnXpBar()).bounds(0, 0, 120, 20).build();
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
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

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
            this.minecraft.setScreen(parent);
        }
    }

    private void cycleStyle() {
        selectedStyle = selectedStyle.next();
        styleButton.setMessage(styleButtonText());
        applyAndSave();
        updatePageState();
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

    private void toggleElementsOnXpBar() {
        selectedElementsOnXpBar = !selectedElementsOnXpBar;
        elementsOnXpBarButton.setMessage(elementsOnXpBarButtonText());
        applyAndSave();
        updateControlStates();
    }

    private Component elementsOnXpBarButtonText() {
        return Component.translatable(selectedElementsOnXpBar ? "locatorbar.option.on" : "locatorbar.option.off");
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
        boolean serverControlled = LocatorBarConfig.hasServerSettings();
        boolean styleEnabled = selectedStyle != LocatorBarStyle.OFF;
        boolean classicStyle = selectedStyle == LocatorBarStyle.CLASSIC;
        boolean reworkedStyle = selectedStyle == LocatorBarStyle.REWORKED;
        boolean canChangeCoordinatesFormat = styleEnabled && !classicStyle && selectedShowCoordinates;
        boolean canChangeDaysOrder = styleEnabled && !classicStyle && selectedShowCoordinates && selectedShowDays;
        boolean canChangeDirectionScale = styleEnabled && selectedShowWorldDirections;
        boolean canChangeHeadSettings = styleEnabled && selectedShowPlayerHeads;
        boolean canChangeWaypoints = styleEnabled && selectedShowWaypoints;

        styleButton.active = !serverControlled;
        scaleSlider.active = reworkedStyle;
        offsetButton.active = reworkedStyle;
        viewAngleSlider.active = reworkedStyle;
        showCoordinatesButton.active = reworkedStyle && !serverControlled;
        elementsOnXpBarButton.active = classicStyle;
        coordinatesFormatButton.active = canChangeCoordinatesFormat;
        showDaysButton.active = reworkedStyle && !serverControlled;
        daysDisplayOrderButton.active = canChangeDaysOrder;

        showWorldDirectionsButton.active = styleEnabled && !serverControlled;
        worldDirectionsScaleSlider.active = canChangeDirectionScale;
        showPlayerHeadsButton.active = styleEnabled && !serverControlled;
        playerHeadsScaleSlider.active = canChangeHeadSettings;
        playerHeadOutlineButton.active = canChangeHeadSettings;
        maxVisiblePlayersSlider.active = canChangeHeadSettings && !serverControlled;

        showWaypointsButton.active = styleEnabled && !serverControlled;
        waypointsScaleSlider.active = canChangeWaypoints;
        maxVisibleWaypointsSlider.active = canChangeWaypoints && !serverControlled;
    }

    private void updatePageState() {
        if (this.list != null) {
            this.list.clearList();

            if (page == 0) {
                this.list.addEntry(Component.translatable("locatorbar.config.field.style"), styleButton);
                if (selectedStyle == LocatorBarStyle.REWORKED) {
                    this.list.addEntry(Component.translatable("locatorbar.config.field.scale"), scaleSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.offset"), offsetButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.view_angle"), viewAngleSlider);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.show_coordinates"), showCoordinatesButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.coordinates_format"), coordinatesFormatButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.show_days"), showDaysButton);
                    this.list.addEntry(Component.translatable("locatorbar.config.field.days_display_order"), daysDisplayOrderButton);
                } else if (selectedStyle == LocatorBarStyle.CLASSIC) {
                    this.list.addEntry(Component.translatable("locatorbar.config.field.elements_on_xp_bar"), elementsOnXpBarButton);
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
                this.list.addEntry(Component.translatable("locatorbar.config.field.waypoints_size"), waypointsScaleSlider);
                this.list.addEntry(Component.translatable("locatorbar.config.field.max_visible_waypoints"), maxVisibleWaypointsSlider);

                if (selectedStyle != LocatorBarStyle.OFF && selectedShowWaypoints) {
                    this.list.addHeaderEntry(Component.translatable("locatorbar.config.header.waypoint_manager"));
                    for (ManagedWaypoint waypoint : collectManagedWaypoints()) {
                        this.list.addWaypointEntry(waypoint);
                    }
                }
            }
        }

        previousPageButton.active = page > 0;
        nextPageButton.active = page < TOTAL_PAGES - 1;
    }

    private List<ManagedWaypoint> collectManagedWaypoints() {
        List<ManagedWaypoint> waypoints = new ArrayList<>();
        Set<UUID> seenIds = new HashSet<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return waypoints;
        }

        String currentWorld = mc.level.dimension().identifier().toString();

        Inventory inventory = mc.player.getInventory();
        //? if >=1.21.11 {
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            addManagedWaypoint(waypoints, seenIds, stack, currentWorld);
        }
        ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
        if (!offhand.isEmpty()) {
            addManagedWaypoint(waypoints, seenIds, offhand, currentWorld);
        }
        //?} else {
        /*for (ItemStack stack : inventory.items) {
            addManagedWaypoint(waypoints, seenIds, stack, currentWorld);
        }
        for (ItemStack stack : inventory.offhand) {
            addManagedWaypoint(waypoints, seenIds, stack, currentWorld);
        }
        *///?}

        LocatorBarConfig.getWaypoints().forEach((id, config) -> {
            if (config.world.equals(currentWorld) && !seenIds.contains(id)) {
                waypoints.add(new ManagedWaypoint(id, config.character, config.color, config.visible, config.world));
                seenIds.add(id);
            }
        });

        return waypoints;
    }

    private void addManagedWaypoint(List<ManagedWaypoint> waypoints, Set<UUID> seenIds, ItemStack stack, String currentWorld) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }

        if (!tracker.target().get().dimension().identifier().toString().equals(currentWorld)) {
            return;
        }

        UUID waypointId = WaypointData.getWaypointId(stack);
        if (waypointId == null) {
            String fallbackSeed = tracker.target().get().dimension().identifier() + "|" + tracker.target().get().pos().toShortString();
            waypointId = UUID.nameUUIDFromBytes(fallbackSeed.getBytes(StandardCharsets.UTF_8));
        }

        if (seenIds.contains(waypointId)) {
            return;
        }

        WaypointConfig config = LocatorBarConfig.getWaypointConfig(waypointId);
        String symbol = config != null ? config.character : WaypointData.getWaypointSymbol(stack);
        int color = config != null ? config.color : (WaypointData.getCustomColor(stack) != null ? WaypointData.getCustomColor(stack) : colorFromWaypointId(waypointId));
        boolean visible = config == null ? !WaypointData.isHidden(stack) : config.visible;

        waypoints.add(new ManagedWaypoint(waypointId, symbol, color, visible, currentWorld));
        seenIds.add(waypointId);
    }

    private static int colorFromWaypointId(UUID waypointId) {
        long hash = waypointId.getMostSignificantBits() ^ waypointId.getLeastSignificantBits();
        float hue = (hash & 0xFFFFL) / 65535.0F;
        float saturation = 0.65F + (((hash >>> 16) & 0xFFL) / 255.0F) * 0.25F;
        float value = 0.8F + (((hash >>> 24) & 0xFFL) / 255.0F) * 0.2F;
        return Mth.hsvToRgb(hue, saturation, value);
    }

    private void applyAndSave() {
        boolean serverControlled = LocatorBarConfig.hasServerSettings();
        if (!serverControlled) {
            LocatorBarConfig.setStyle(selectedStyle);
        }
        LocatorBarConfig.setScale(selectedScale);
        LocatorBarConfig.setOffset(selectedOffset);
        LocatorBarConfig.setViewAngle(selectedViewAngle);
        if (!serverControlled) {
            LocatorBarConfig.setShowCoordinates(selectedShowCoordinates);
        }
        LocatorBarConfig.setElementsOnXpBar(selectedElementsOnXpBar);
        LocatorBarConfig.setCoordinatesFormat(selectedCoordinatesFormat);
        if (!serverControlled) {
            LocatorBarConfig.setShowDays(selectedShowDays);
        }
        LocatorBarConfig.setDaysDisplayOrder(selectedDaysDisplayOrder);
        if (!serverControlled) {
            LocatorBarConfig.setShowWorldDirections(selectedShowWorldDirections);
        }
        LocatorBarConfig.setWorldDirectionsScale(selectedWorldDirectionsScale);
        if (!serverControlled) {
            LocatorBarConfig.setShowPlayerHeads(selectedShowPlayerHeads);
        }
        LocatorBarConfig.setPlayerHeadsScale(selectedPlayerHeadsScale);
        LocatorBarConfig.setPlayerHeadOutline(selectedPlayerHeadOutline);
        if (!serverControlled) {
            LocatorBarConfig.setMaxVisiblePlayers(selectedMaxVisiblePlayers);
            LocatorBarConfig.setShowWaypoints(selectedShowWaypoints);
        }
        LocatorBarConfig.setWaypointsScale(selectedWaypointsScale);
        if (!serverControlled) {
            LocatorBarConfig.setMaxVisibleWaypoints(selectedMaxVisibleWaypoints);
        }
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

    private record ManagedWaypoint(UUID id, String symbol, int color, boolean visible, String world) {
    }

    private final class ConfigList extends ContainerObjectSelectionList<ConfigList.AbstractEntry> {
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

        public void addWaypointEntry(ManagedWaypoint waypoint) {
            super.addEntry(new WaypointEntry(waypoint));
        }

        public void addHeaderEntry(Component label) {
            super.addEntry(new HeaderEntry(label));
        }

        @Override
        public int getRowWidth() {
            return 340;
        }

        //? if >=1.21.11 {
        private int getScrollbarPosition() {
            return this.width / 2 + 160;
        }
        //?} else {
        /*protected int getScrollbarPosition() {
            return this.width / 2 + 160;
        }
        *///?}

        abstract class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
            //? if >=26.1 {
            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int top = this.getContentY();
                int height = this.getContentHeight();
                renderEntry(guiGraphics, top, height, mouseX, mouseY, partialTick);
            }
            //?} elif >=1.21.11 {
            /*@Override
            public void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int top = this.getContentY();
                int height = this.getContentHeight();
                renderEntry(guiGraphics, top, height, mouseX, mouseY, partialTick);
            }
            *///?} else {
            /*@Override
            public void render(GuiGraphicsExtractor guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                renderEntry(guiGraphics, top, height, mouseX, mouseY, partialTick);
            }
            *///?}

            protected abstract void renderEntry(GuiGraphicsExtractor guiGraphics, int top, int height, int mouseX, int mouseY, float partialTick);
        }

        class HeaderEntry extends AbstractEntry {
            private final Component label;

            public HeaderEntry(Component label) {
                this.label = label;
            }

            @Override
            protected void renderEntry(GuiGraphicsExtractor guiGraphics, int top, int height, int mouseX, int mouseY, float partialTick) {
                int centerX = LocatorBarConfigScreen.this.width / 2;
                int centerY = top + (height - LocatorBarConfigScreen.this.font.lineHeight) / 2;
                guiGraphics.centeredText(LocatorBarConfigScreen.this.font, label, centerX, centerY, 0xFFFFFFFF);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return ImmutableList.of();
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return ImmutableList.of();
            }
        }

        class Entry extends AbstractEntry {
            private final Component label;
            private final AbstractWidget widget;
            private final List<AbstractWidget> children;

            public Entry(Component label, AbstractWidget widget) {
                this.label = label;
                this.widget = widget;
                this.children = ImmutableList.of(widget);
            }

            @Override
            protected void renderEntry(GuiGraphicsExtractor guiGraphics, int top, int height, int mouseX, int mouseY, float partialTick) {
                int centerY = top + (height - LocatorBarConfigScreen.this.font.lineHeight) / 2;

                guiGraphics.text(LocatorBarConfigScreen.this.font, label, LocatorBarConfigScreen.this.width / 2 - 138, centerY, 0xFFFFFFFF, false);

                widget.setX(LocatorBarConfigScreen.this.width / 2 + 20);
                widget.setY(top);
                //? if >=26.1
                widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                //? if <26.1
                /*widget.render(guiGraphics, mouseX, mouseY, partialTick);*/
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

        class WaypointEntry extends AbstractEntry {
            private final ManagedWaypoint waypoint;
            private final EditBox symbolBox;
            private final EditBox colorBox;
            private final Button visibilityButton;
            private final Button deleteButton;
            private final List<AbstractWidget> children;

            public WaypointEntry(ManagedWaypoint waypoint) {
                this.waypoint = waypoint;

                this.symbolBox = new EditBox(LocatorBarConfigScreen.this.font, 0, 0, 20, 20, Component.empty());
                this.symbolBox.setValue(waypoint.symbol != null ? waypoint.symbol : "");
                this.symbolBox.setMaxLength(1);
                this.symbolBox.setResponder(value -> updateWaypoint());

                this.colorBox = new EditBox(LocatorBarConfigScreen.this.font, 0, 0, 50, 20, Component.empty());
                this.colorBox.setValue(String.format("%06X", waypoint.color & 0xFFFFFF));
                this.colorBox.setMaxLength(6);
                this.colorBox.setResponder(value -> updateWaypoint());

                this.visibilityButton = Button.builder(
                        Component.translatable(waypoint.visible ? "locatorbar.option.on" : "locatorbar.option.off"),
                        button -> {
                            toggleVisibility();
                        }
                ).bounds(0, 0, 40, 20).build();

                this.deleteButton = Button.builder(
                        Component.literal("X"),
                        button -> {
                            deleteWaypoint();
                        }
                ).bounds(0, 0, 20, 20).build();

                this.children = ImmutableList.of(symbolBox, colorBox, visibilityButton, deleteButton);
            }

            private void toggleVisibility() {
                boolean next = visibilityButton.getMessage().getString().equals(Component.translatable("locatorbar.option.off").getString());
                visibilityButton.setMessage(Component.translatable(next ? "locatorbar.option.on" : "locatorbar.option.off"));
                updateWaypoint();
            }

            private void deleteWaypoint() {
                LocatorBarConfig.removeWaypointConfig(waypoint.id);
                LocatorBarConfig.save();
                LocatorBarConfigScreen.this.updatePageState();
            }

            private void updateWaypoint() {
                String symbol = symbolBox.getValue();
                int color;
                try {
                    color = Integer.parseInt(colorBox.getValue(), 16);
                } catch (NumberFormatException e) {
                    color = colorFromWaypointId(waypoint.id);
                }
                boolean visible = visibilityButton.getMessage().getString().equals(Component.translatable("locatorbar.option.on").getString());

                LocatorBarConfig.setWaypointConfig(waypoint.id, new WaypointConfig(waypoint.world, color, symbol, visible));
                LocatorBarConfig.save();
            }

            @Override
            protected void renderEntry(GuiGraphicsExtractor guiGraphics, int top, int height, int mouseX, int mouseY, float partialTick) {
                int centerX = LocatorBarConfigScreen.this.width / 2;
                int previewX = centerX - 138;
                int previewSize = 20;
                int previewY = top + (height - previewSize) / 2;

                // Preview
                int color;
                try {
                    color = Integer.parseInt(colorBox.getValue(), 16);
                } catch (NumberFormatException e) {
                    color = colorFromWaypointId(waypoint.id);
                }
                String symbol = symbolBox.getValue();
                if (symbol.isEmpty()) {
                    symbol = waypoint.symbol;
                }

                RenderCompat.push(guiGraphics);
                RenderCompat.translate(guiGraphics, previewX, previewY);
                RenderCompat.blitTinted(
                        guiGraphics,
                        WAYPOINT_TEXTURE,
                        0,
                        0,
                        0,
                        0,
                        previewSize,
                        previewSize,
                        36,
                        36,
                        36,
                        36,
                        0xFF000000 | color
                );

                if (!symbol.isEmpty()) {
                    float dynamicTextScale = 0.75F * (previewSize / 14.0F);
                    float textWidth = LocatorBarConfigScreen.this.font.width(symbol) * dynamicTextScale;
                    float textHeight = LocatorBarConfigScreen.this.font.lineHeight * dynamicTextScale;
                    float textX = ((previewSize - textWidth) / 2.0F) + 0.45F;
                    float textY = (previewSize - textHeight) / 2.0F;
                    RenderCompat.push(guiGraphics);
                    RenderCompat.translate(guiGraphics, textX, textY);
                    RenderCompat.scale(guiGraphics, dynamicTextScale, dynamicTextScale);
                    RenderCompat.text(guiGraphics, symbol, 0, 0, 0xFFFFFFFF, false);
                    RenderCompat.pop(guiGraphics);
                }
                RenderCompat.pop(guiGraphics);

                symbolBox.setX(centerX - 100);
                symbolBox.setY(top);
                //? if >=26.1
                symbolBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                //? if <26.1
                /*symbolBox.render(guiGraphics, mouseX, mouseY, partialTick);*/

                colorBox.setX(centerX - 70);
                colorBox.setY(top);
                //? if >=26.1
                colorBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                //? if <26.1
                /*colorBox.render(guiGraphics, mouseX, mouseY, partialTick);*/

                visibilityButton.setX(centerX - 10);
                visibilityButton.setY(top);
                //? if >=26.1
                visibilityButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                //? if <26.1
                /*visibilityButton.render(guiGraphics, mouseX, mouseY, partialTick);*/

                deleteButton.setX(centerX + 35);
                deleteButton.setY(top);
                //? if >=26.1
                deleteButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
                //? if <26.1
                /*deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);*/
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