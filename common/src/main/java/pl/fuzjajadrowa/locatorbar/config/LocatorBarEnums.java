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

    public enum LocatorBarOffset {
        CENTER("locatorbar.offset.center"),
        LEFT("locatorbar.offset.left"),
        RIGHT("locatorbar.offset.right");

        private final String translationKey;

        LocatorBarOffset(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public LocatorBarOffset next() {
            return switch (this) {
                case CENTER -> LEFT;
                case LEFT -> RIGHT;
                case RIGHT -> CENTER;
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
}
