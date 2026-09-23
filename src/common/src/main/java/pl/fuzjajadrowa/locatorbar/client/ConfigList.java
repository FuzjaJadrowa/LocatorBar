package pl.fuzjajadrowa.locatorbar.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.Identifier;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig.WaypointConfig;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;

import java.util.List;

import pl.fuzjajadrowa.locatorbar.client.WaypointConfigPage.ManagedWaypoint;

final class ConfigList extends ContainerObjectSelectionList<ConfigList.AbstractEntry> {
    private final net.minecraft.client.gui.Font font;
    private final Runnable refreshPage;
    private static final Identifier WAYPOINT_TEXTURE = Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "textures/gui/waypoint.png");
    public ConfigList(Minecraft minecraft, int width, int height, int y, int itemHeight, int screenHeight, Runnable refreshPage) {
        //? if >=1.21 {
        super(minecraft, width, height, y, itemHeight);
        //?} else {
        /*super(minecraft, width, screenHeight, y, y + height, itemHeight);
        *///?}
        this.font = minecraft.font;
        this.refreshPage = refreshPage;
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
            int centerX = ConfigList.this.width / 2;
            int centerY = top + (height - font.lineHeight) / 2;
            guiGraphics.centeredText(font, label, centerX, centerY, 0xFFFFFFFF);
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
            int centerY = top + (height - font.lineHeight) / 2;

            guiGraphics.text(font, label, ConfigList.this.width / 2 - 138, centerY, 0xFFFFFFFF, false);

            widget.setX(ConfigList.this.width / 2 + 20);
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

            String initialSymbol = waypoint.symbol();
            if ((initialSymbol == null || initialSymbol.isEmpty()) && waypoint.index() > 0) {
                initialSymbol = Integer.toString(waypoint.index());
            }

            this.symbolBox = new EditBox(font, 0, 0, 20, 20, Component.empty());
            this.symbolBox.setValue(initialSymbol != null ? initialSymbol : "");
            this.symbolBox.setMaxLength(4);
            this.symbolBox.setResponder(value -> updateWaypoint());
            saveDefaultWaypointConfigIfNeeded(initialSymbol);

            this.colorBox = new EditBox(font, 0, 0, 50, 20, Component.empty());
            this.colorBox.setValue(String.format("%06X", waypoint.color() & 0xFFFFFF));
            this.colorBox.setMaxLength(6);
            this.colorBox.setResponder(value -> updateWaypoint());

            this.visibilityButton = Button.builder(
                    Component.translatable(waypoint.visible() ? "locatorbar.option.on" : "locatorbar.option.off"),
                    button -> {
                        toggleVisibility();
                    }
            ).bounds(0, 0, 40, 20).build();

            this.deleteButton = Button.builder(
                    Component.translatable("locatorbar.config.button.delete"),
                    button -> {
                        deleteWaypoint();
                    }
            ).bounds(0, 0, 50, 20).build();

            this.children = ImmutableList.of(symbolBox, colorBox, visibilityButton, deleteButton);
        }

        private void toggleVisibility() {
            boolean next = visibilityButton.getMessage().getString().equals(Component.translatable("locatorbar.option.off").getString());
            visibilityButton.setMessage(Component.translatable(next ? "locatorbar.option.on" : "locatorbar.option.off"));
            updateWaypoint();
        }

        private void deleteWaypoint() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                WaypointData.unlinkWaypoint(mc.player, waypoint.id());
            }

            LocatorBarConfig.removeWaypointConfig(waypoint.id());
            LocatorBarConfig.save();

            refreshPage.run();
        }

        private void updateWaypoint() {
            String symbol = symbolBox.getValue();
            int color;
            try {
                color = Integer.parseInt(colorBox.getValue(), 16);
            } catch (NumberFormatException e) {
                color = LocatorBarHudHelper.colorFromWaypointId(waypoint.id());
            }
            boolean visible = visibilityButton.getMessage().getString().equals(Component.translatable("locatorbar.option.on").getString());

            LocatorBarConfig.setWaypointConfig(waypoint.id(), new WaypointConfig(waypoint.world(), color, symbol, visible));
            LocatorBarConfig.save();
        }

        private void saveDefaultWaypointConfigIfNeeded(String initialSymbol) {
            if (LocatorBarConfig.getWaypointConfig(waypoint.id()) != null || initialSymbol == null || initialSymbol.isEmpty()) {
                return;
            }

            LocatorBarConfig.setWaypointConfig(waypoint.id(), new WaypointConfig(waypoint.world(), waypoint.color(), initialSymbol, waypoint.visible()));
            LocatorBarConfig.save();
        }

        @Override
        protected void renderEntry(GuiGraphicsExtractor guiGraphics, int top, int height, int mouseX, int mouseY, float partialTick) {
            int centerX = ConfigList.this.width / 2;

            int totalWidth = 220;
            int startX = centerX - (totalWidth / 2);

            int previewX = startX;
            int previewSize = 20;
            int previewY = top + (height - previewSize) / 2;

            int color;
            try {
                color = Integer.parseInt(colorBox.getValue(), 16);
            } catch (NumberFormatException e) {
                color = LocatorBarHudHelper.colorFromWaypointId(waypoint.id());
            }
            String symbol = symbolBox.getValue();
            if (symbol.isEmpty()) {
                if (waypoint.symbol() != null && !waypoint.symbol().isEmpty()) {
                    symbol = waypoint.symbol();
                } else if (waypoint.index() > 0) {
                    symbol = Integer.toString(waypoint.index());
                }
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
                float textWidth = font.width(symbol) * dynamicTextScale;
                float textHeight = font.lineHeight * dynamicTextScale;
                float textX = ((previewSize - textWidth) / 2.0F) + 0.45F;
                float textY = (previewSize - textHeight) / 2.0F;
                RenderCompat.push(guiGraphics);
                RenderCompat.translate(guiGraphics, textX, textY);
                RenderCompat.scale(guiGraphics, dynamicTextScale, dynamicTextScale);
                RenderCompat.text(guiGraphics, symbol, 0, 0, 0xFFFFFFFF, false);
                RenderCompat.pop(guiGraphics);
            }
            RenderCompat.pop(guiGraphics);

            int currentX = startX + 20 + 10;
            symbolBox.setX(currentX);
            symbolBox.setY(top);
            //? if >=26.1
            symbolBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            //? if <26.1
            /*symbolBox.render(guiGraphics, mouseX, mouseY, partialTick);*/

            currentX += 20 + 10;
            colorBox.setX(currentX);
            colorBox.setY(top);
            //? if >=26.1
            colorBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            //? if <26.1
            /*colorBox.render(guiGraphics, mouseX, mouseY, partialTick);*/

            currentX += 50 + 10;
            visibilityButton.setX(currentX);
            visibilityButton.setY(top);
            //? if >=26.1
            visibilityButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            //? if <26.1
            /*visibilityButton.render(guiGraphics, mouseX, mouseY, partialTick);*/

            currentX += 40 + 10;
            deleteButton.setX(currentX);
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