package pl.fuzjajadrowa.locatorbar.config;

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