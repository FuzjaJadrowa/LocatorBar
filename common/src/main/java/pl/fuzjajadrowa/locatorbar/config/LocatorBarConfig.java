package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarOffset;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocatorBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "locatorbar.json");
    private static LocatorBarConfigData data = new LocatorBarConfigData();

    private LocatorBarConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            LocatorBarConfigData loaded = GSON.fromJson(reader, LocatorBarConfigData.class);
            data = loaded == null ? new LocatorBarConfigData() : loaded;
            if (data.style == null) {
                data.style = LocatorBarStyle.REWORKED;
            }
            if (data.offset == null) {
                data.offset = LocatorBarOffset.CENTER;
            }
            if (data.coordinatesFormat == null) {
                data.coordinatesFormat = CoordinatesFormat.XYZ;
            }
            data.scale = clamp(data.scale, 0.5F, 2.0F);
            data.viewAngle = clamp(data.viewAngle, 30.0F, 180.0F);
        } catch (IOException | JsonParseException exception) {
            data = new LocatorBarConfigData();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
            // Keep runtime behavior stable even if saving fails.
        }
    }

    public static LocatorBarStyle getStyle() {
        return data.style;
    }

    public static void setStyle(LocatorBarStyle style) {
        data.style = style;
    }

    public static boolean isEnabled() {
        return getStyle() != LocatorBarStyle.OFF;
    }

    public static float getScale() {
        return data.scale;
    }

    public static void setScale(float scale) {
        data.scale = clamp(scale, 0.5F, 2.0F);
    }

    public static LocatorBarOffset getOffset() {
        return data.offset;
    }

    public static void setOffset(LocatorBarOffset offset) {
        data.offset = offset;
    }

    public static float getViewAngle() {
        return data.viewAngle;
    }

    public static void setViewAngle(float viewAngle) {
        data.viewAngle = clamp(viewAngle, 30.0F, 180.0F);
    }

    public static boolean isShowCoordinates() {
        return data.showCoordinates;
    }

    public static void setShowCoordinates(boolean showCoordinates) {
        data.showCoordinates = showCoordinates;
    }

    public static CoordinatesFormat getCoordinatesFormat() {
        return data.coordinatesFormat;
    }

    public static void setCoordinatesFormat(CoordinatesFormat coordinatesFormat) {
        data.coordinatesFormat = coordinatesFormat;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class LocatorBarConfigData {
        @SerializedName("style")
        private LocatorBarStyle style = LocatorBarStyle.REWORKED;

        @SerializedName("scale")
        private float scale = 1.0F;

        @SerializedName("offset")
        private LocatorBarOffset offset = LocatorBarOffset.CENTER;

        @SerializedName("viewAngle")
        private float viewAngle = 90.0F;

        @SerializedName("showCoordinates")
        private boolean showCoordinates = true;

        @SerializedName("coordinatesFormat")
        private CoordinatesFormat coordinatesFormat = CoordinatesFormat.XYZ;
    }
}
