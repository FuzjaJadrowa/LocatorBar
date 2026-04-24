package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void locatorbar$addConfigShortcut(CallbackInfo ci) {
        Button locatorBarButton = addRenderableWidget(
                Button.builder(Component.literal("Locator Bar"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new LocatorBarConfigScreen((Screen) (Object) this));
                    }
                }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build()
        );

        AbstractWidget fovWidget = findWidgetByKey("options.fov");
        AbstractWidget skinWidget = findWidgetByKey("options.skinCustomisation");

        if (fovWidget != null && skinWidget != null) {
            locatorBarButton.setX(Math.min(fovWidget.getX(), skinWidget.getX()));
            locatorBarButton.setY(skinWidget.getY() - 24);
        } else if (skinWidget != null) {
            locatorBarButton.setX(skinWidget.getX());
            locatorBarButton.setY(skinWidget.getY() - 24);
        } else if (fovWidget != null) {
            locatorBarButton.setX(fovWidget.getX());
            locatorBarButton.setY(fovWidget.getY() + 24);
        }
    }

    private AbstractWidget findWidgetByKey(String key) {
        for (GuiEventListener listener : this.children()) {
            if (!(listener instanceof AbstractWidget widget)) {
                continue;
            }
            if (!(widget.getMessage().getContents() instanceof TranslatableContents translatable)) {
                continue;
            }
            if (key.equals(translatable.getKey())) {
                return widget;
            }
        }
        return null;
    }
}