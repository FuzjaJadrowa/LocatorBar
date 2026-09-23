package pl.fuzjajadrowa.locatorbar.client;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;

final class ConfigScreenState {
    LocatorBarStyle selectedStyle;
    float selectedScale;
    int selectedCustomOffsetX;
    int selectedCustomOffsetY;
    float selectedViewAngle;
    boolean selectedShowCoordinates;
    boolean selectedElementsOnXpBar;
    CoordinatesFormat selectedCoordinatesFormat;
    boolean selectedShowDays;
    DaysDisplayOrder selectedDaysDisplayOrder;
    boolean selectedShowWorldDirections;
    float selectedWorldDirectionsScale;
    PlayerMarkerType selectedPlayerMarkerType;
    float selectedPlayerMarkersScale;
    boolean selectedPlayerMarkerOutline;
    int selectedMaxVisiblePlayers;
    boolean selectedShowWaypoints;
    boolean selectedShowDeathWaypoint;
    float selectedWaypointsScale;
    int selectedMaxVisibleWaypoints;

    ConfigScreenState() {
        this.selectedStyle = LocatorBarConfig.getStyle();
        this.selectedScale = LocatorBarConfig.getScale();
        this.selectedCustomOffsetX = LocatorBarConfig.getCustomOffsetX();
        this.selectedCustomOffsetY = LocatorBarConfig.getCustomOffsetY();
        this.selectedViewAngle = LocatorBarConfig.getViewAngle();
        this.selectedShowCoordinates = LocatorBarConfig.isShowCoordinates();
        this.selectedElementsOnXpBar = LocatorBarConfig.isElementsOnXpBar();
        this.selectedCoordinatesFormat = LocatorBarConfig.getCoordinatesFormat();
        this.selectedShowDays = LocatorBarConfig.isShowDays();
        this.selectedDaysDisplayOrder = LocatorBarConfig.getDaysDisplayOrder();
        this.selectedShowWorldDirections = LocatorBarConfig.isShowWorldDirections();
        this.selectedWorldDirectionsScale = LocatorBarConfig.getWorldDirectionsScale();
        this.selectedPlayerMarkerType = LocatorBarConfig.getPlayerMarkerType();
        this.selectedPlayerMarkersScale = LocatorBarConfig.getPlayerMarkersScale();
        this.selectedPlayerMarkerOutline = LocatorBarConfig.isPlayerMarkerOutline();
        this.selectedMaxVisiblePlayers = LocatorBarConfig.getMaxVisiblePlayers();
        this.selectedShowWaypoints = LocatorBarConfig.isShowWaypoints();
        this.selectedShowDeathWaypoint = LocatorBarConfig.isShowDeathWaypoint();
        this.selectedWaypointsScale = LocatorBarConfig.getWaypointsScale();
        this.selectedMaxVisibleWaypoints = LocatorBarConfig.getMaxVisibleWaypoints();
    }

    void apply() {
        boolean serverControlled = LocatorBarConfig.hasServerSettings();
        if (!serverControlled) {
            LocatorBarConfig.setStyle(selectedStyle);
        }
        LocatorBarConfig.setScale(selectedScale);
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
            LocatorBarConfig.setPlayerMarkerType(selectedPlayerMarkerType);
        }
        LocatorBarConfig.setPlayerMarkersScale(selectedPlayerMarkersScale);
        LocatorBarConfig.setPlayerMarkerOutline(selectedPlayerMarkerOutline);
        if (!serverControlled) {
            LocatorBarConfig.setMaxVisiblePlayers(selectedMaxVisiblePlayers);
            LocatorBarConfig.setShowWaypoints(selectedShowWaypoints);
        }
        LocatorBarConfig.setWaypointsScale(selectedWaypointsScale);
        if (!serverControlled) {
            LocatorBarConfig.setMaxVisibleWaypoints(selectedMaxVisibleWaypoints);
            LocatorBarConfig.setShowDeathWaypoint(selectedShowDeathWaypoint);
        }
        LocatorBarConfig.setCustomOffsetX(selectedCustomOffsetX);
        LocatorBarConfig.setCustomOffsetY(selectedCustomOffsetY);
    }
}