package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData.CompassWaypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LocatorBarHudHelper {
    private static final UUID DEATH_WAYPOINT_ID = new UUID(0L, 0L);
    private LocatorBarHudHelper() {
    }

    public record WaypointMarker(UUID waypointId, float directionYaw, int rgbColor, int index, String symbol, boolean isDeath) {
    }

    public static List<WaypointMarker> collectWaypointMarkers(Player localPlayer) {
        Minecraft mc = Minecraft.getInstance();
        LocatorBarUtils.CameraView camera = LocatorBarUtils.getCameraView(mc, localPlayer);
        List<WaypointMarker> markers = new ArrayList<>();
        java.util.Set<UUID> seenWaypointIds = new java.util.HashSet<>();
        String currentDimension = localPlayer.level().dimension().identifier().toString();

        for (CompassWaypoint compass : ClientWaypointInventory.waypoints(localPlayer)) {
            addWaypointMarker(markers, seenWaypointIds, compass, camera, currentDimension);
        }

        if (LocatorBarConfig.isShowDeathWaypoint()) {
            if (hasRecoveryCompass(localPlayer)) {
                GlobalPos lastDeath = localPlayer.getLastDeathLocation().orElse(null);
                if (lastDeath != null && lastDeath.dimension().equals(localPlayer.level().dimension())) {
                    double dx = lastDeath.pos().getX() + 0.5D - camera.x();
                    double dz = lastDeath.pos().getZ() + 0.5D - camera.z();
                    if (dx * dx + dz * dz >= 1.0E-6D) {
                        float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        markers.add(new WaypointMarker(
                                DEATH_WAYPOINT_ID,
                                LocatorBarUtils.wrapTo180(directionYaw),
                                0xFFFFFF,
                                -1,
                                "",
                                true
                        ));
                    }
                }
            }
        }

        //? if >=26.2 {
        if (mc.getConnection() != null) {
            var waypointManager = mc.getConnection().getWaypointManager();
            if (waypointManager != null) {
                float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                var mainCamera = mc.gameRenderer.mainCamera();
                var cameraEntity = camera.entity();

                waypointManager.forEachWaypoint(cameraEntity, trackedWaypoint -> {
                    trackedWaypoint.id().right().ifPresent(name -> {
                        double yawDiffDeg = trackedWaypoint.yawAngleToCamera(localPlayer.level(), mainCamera, entity -> partialTicks);
                        float directionYaw = LocatorBarUtils.wrapTo180(camera.yaw() + (float) yawDiffDeg);

                        int color = 0xFFFFFF;
                        var icon = trackedWaypoint.icon();
                        if (icon != null && icon.color != null && icon.color.isPresent()) {
                            color = LocatorBarUtils.normalizeTeamColor(icon.color.get());
                        } else {
                            int argb = (255 << 24) | (name.hashCode() & 0x00FFFFFF);
                            color = LocatorBarUtils.setBrightness(argb, 0.9F);
                        }

                        UUID dummyId = UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        if (seenWaypointIds.add(dummyId)) {
                            markers.add(new WaypointMarker(
                                    dummyId,
                                    LocatorBarUtils.wrapTo180(directionYaw),
                                    color,
                                    -1,
                                    "",
                                    false
                            ));
                        }
                    });
                });
            }
        }
        //?}

        markers.sort((m1, m2) -> {
            if (m1.isDeath() && !m2.isDeath()) return 1;
            if (!m1.isDeath() && m2.isDeath()) return -1;
            int idx1 = m1.index() > 0 ? m1.index() : Integer.MAX_VALUE;
            int idx2 = m2.index() > 0 ? m2.index() : Integer.MAX_VALUE;
            if (idx1 != idx2) {
                return Integer.compare(idx1, idx2);
            }
            return m1.waypointId().compareTo(m2.waypointId());
        });
        return markers;
    }

    private static void addWaypointMarker(
            List<WaypointMarker> markers,
            java.util.Set<UUID> seenWaypointIds,
            CompassWaypoint compass,
            LocatorBarUtils.CameraView camera,
            String currentDimension
    ) {
        if (compass == null || !compass.dimensionId().equals(currentDimension)) {
            return;
        }

        double dx = compass.pos().getX() + 0.5D - camera.x();
        double dz = compass.pos().getZ() + 0.5D - camera.z();
        if (dx * dx + dz * dz < 1.0E-6D) {
            return;
        }

        UUID waypointId = compass.id();
        if (waypointId == null || !seenWaypointIds.add(waypointId)) {
            return;
        }

        LocatorBarConfig.WaypointConfig config = LocatorBarConfig.getWaypointConfig(waypointId);
        boolean visible = config == null ? !compass.hidden() : config.visible;
        if (!visible) {
            return;
        }

        int index = compass.index();
        int color;
        if (config != null) {
            color = config.color;
        } else {
            Integer customColor = compass.customColor();
            color = customColor == null ? colorFromWaypointId(waypointId) : customColor;
        }

        String symbol = config != null ? config.character : compass.symbol();

        float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        markers.add(new WaypointMarker(waypointId, LocatorBarUtils.wrapTo180(directionYaw), color, index, symbol, false));
    }

    public static int colorFromWaypointId(UUID waypointId) {
        return LocatorBarUtils.colorFromId(waypointId, 0.65F, 0.25F, 0.80F, 0.20F);
    }

    public static List<PlayerLocatorClient.Marker> collectPlayerMarkers(Player localPlayer) {
        float start = LocatorBarConfig.getPlayerMarkerFadeStartDistance();
        float end = LocatorBarConfig.getPlayerMarkerFadeToMinDistance();
        float hide = LocatorBarConfig.getPlayerMarkerHideDistance();
        float minAlpha = LocatorBarConfig.getPlayerMarkerMinAlpha();
        return PlayerLocatorClient.collectMarkers(localPlayer,
                distance -> pl.fuzjajadrowa.locatorbar.util.MarkerMath.playerAlpha(distance, start, end, hide, minAlpha));
    }

    public static boolean hasRecoveryCompass(Player player) {
        return ClientWaypointInventory.hasRecoveryCompass(player);
    }

    public static boolean hasAnyBarElements(Player player) {
        if (LocatorBarConfig.isShowWorldDirections()) {
            return true;
        }
        if (LocatorBarConfig.getPlayerMarkerType() != pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType.OFF
                && !collectPlayerMarkers(player).isEmpty()) {
            return true;
        }
        return LocatorBarConfig.isShowWaypoints() && !collectWaypointMarkers(player).isEmpty();
    }
}