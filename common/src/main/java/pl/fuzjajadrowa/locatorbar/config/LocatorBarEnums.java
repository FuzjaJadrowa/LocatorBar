package pl.fuzjajadrowa.locatorbar.config;

public final class LocatorBarEnums {
    private LocatorBarEnums() {
    }

    public enum LocatorBarStyle {
        REWORKED("Reworked"),
        OFF("Off");

        private final String label;

        LocatorBarStyle(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public LocatorBarStyle next() {
            return this == REWORKED ? OFF : REWORKED;
        }
    }

    public enum LocatorBarOffset {
        CENTER("Center"),
        LEFT("Left"),
        RIGHT("Right");

        private final String label;

        LocatorBarOffset(String label) {
            this.label = label;
        }

        public String label() {
            return label;
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
        XYZ("XYZ"),
        XZ("XZ");

        private final String label;

        CoordinatesFormat(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public CoordinatesFormat next() {
            return this == XYZ ? XZ : XYZ;
        }
    }

    public enum DaysDisplayOrder {
        DAYS_UNDER_COORDS("Days under coords"),
        COORDS_UNDER_DAYS("Coords under days");

        private final String label;

        DaysDisplayOrder(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public DaysDisplayOrder next() {
            return this == DAYS_UNDER_COORDS ? COORDS_UNDER_DAYS : DAYS_UNDER_COORDS;
        }
    }
}