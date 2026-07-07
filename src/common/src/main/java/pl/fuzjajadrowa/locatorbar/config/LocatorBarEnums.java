package pl.fuzjajadrowa.locatorbar.config;

public final class LocatorBarEnums {
    private LocatorBarEnums() {
    }

    public enum LocatorBarStyle {
        REWORKED("locatorbar.style.reworked"),
        CLASSIC("locatorbar.style.classic"),
        OFF("locatorbar.style.off");

        private final String translationKey;

        LocatorBarStyle(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public LocatorBarStyle next() {
            return switch (this) {
                case REWORKED -> CLASSIC;
                case CLASSIC -> OFF;
                case OFF -> REWORKED;
            };
        }
    }


    public enum CoordinatesFormat {
        XYZ("locatorbar.coordinates_format.xyz"),
        XZ("locatorbar.coordinates_format.xz");

        private final String translationKey;

        CoordinatesFormat(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public CoordinatesFormat next() {
            return this == XYZ ? XZ : XYZ;
        }
    }

    public enum DaysDisplayOrder {
        DAYS_UNDER_COORDS("locatorbar.days_order.days_under_coords"),
        COORDS_UNDER_DAYS("locatorbar.days_order.coords_under_days");

        private final String translationKey;

        DaysDisplayOrder(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public DaysDisplayOrder next() {
            return this == DAYS_UNDER_COORDS ? COORDS_UNDER_DAYS : DAYS_UNDER_COORDS;
        }
    }

    public enum PlayerMarkerType {
        HEADS("locatorbar.player_marker.heads"),
        DOTS("locatorbar.player_marker.dots"),
        OFF("locatorbar.player_marker.off");

        private final String translationKey;

        PlayerMarkerType(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public PlayerMarkerType next() {
            return switch (this) {
                case HEADS -> DOTS;
                case DOTS -> OFF;
                case OFF -> HEADS;
            };
        }
    }
}