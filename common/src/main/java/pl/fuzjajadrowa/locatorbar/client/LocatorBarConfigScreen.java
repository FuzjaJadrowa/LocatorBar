package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarStyle;

public final class LocatorBarConfigScreen extends Screen {
    private static final int TOTAL_PAGES = 1;
    private final Screen parent;
    private LocatorBarStyle selectedStyle;
    private int page = 0;
    private Button styleButton;

    public LocatorBarConfigScreen(Screen parent) {
        super(Component.literal("Locator bar config"));
        this.parent = parent;
        this.selectedStyle = LocatorBarConfig.getStyle();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int controlsY = this.height - 28;

        styleButton = Button.builder(styleButtonText(), button -> cycleStyle())
                .bounds(centerX + 50, 76, 90, 20)
                .build();
        addRenderableWidget(styleButton);

        addRenderableWidget(
                Button.builder(Component.literal("Back"), button -> onClose())
                        .bounds(centerX - 150, controlsY, 70, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.literal("Save"), button -> saveAndClose())
                        .bounds(centerX - 35, controlsY, 70, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.literal("<"), button -> previousPage())
                        .bounds(centerX + 80, controlsY, 30, 20)
                        .build()
        ).active = false;

        addRenderableWidget(
                Button.builder(Component.literal(">"), button -> nextPage())
                        .bounds(centerX + 116, controlsY, 30, 20)
                        .build()
        ).active = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        guiGraphics.drawCenteredString(this.font, Component.literal("Locator bar config"), centerX, 15, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.literal("1. General"), centerX - 140, 48, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Locator bar style"), centerX - 140, 82, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Page 1/1"), centerX + 86, this.height - 40, 0xA0A0A0, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void saveAndClose() {
        LocatorBarConfig.setStyle(selectedStyle);
        LocatorBarConfig.save();
        onClose();
    }

    private void cycleStyle() {
        selectedStyle = selectedStyle.next();
        if (styleButton != null) {
            styleButton.setMessage(styleButtonText());
        }
    }

    private Component styleButtonText() {
        return Component.literal(selectedStyle.label());
    }

    private void previousPage() {
        page = Math.max(0, page - 1);
    }

    private void nextPage() {
        page = Math.min(TOTAL_PAGES - 1, page + 1);
    }
}