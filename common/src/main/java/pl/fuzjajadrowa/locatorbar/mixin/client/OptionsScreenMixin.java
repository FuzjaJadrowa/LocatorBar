package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    @Unique
    private Button locatorbar$button;
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void locatorbar$addConfigShortcut(CallbackInfo ci) {
        this.locatorbar$button = addRenderableWidget(
                Button.builder(Component.translatable("locatorbar.options.button"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new LocatorBarConfigScreen(this));
                    }
                }).bounds(0, 0, 150, 20).build()
        );
        locatorbar$updateButtonPosition();
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void locatorbar$onRepositionElements(CallbackInfo ci) {
        locatorbar$updateButtonPosition();
    }

    @Unique
    private void locatorbar$updateButtonPosition() {
        if (this.locatorbar$button == null) {
            return;
        }

        AbstractWidget fovWidget = locatorbar$findWidgetByKey("options.fov");
        AbstractWidget skinWidget = locatorbar$findWidgetByKey("options.skinCustomisation");

        if (fovWidget != null && skinWidget != null) {
            this.locatorbar$button.setX(Math.min(fovWidget.getX(), skinWidget.getX()));
            this.locatorbar$button.setY(skinWidget.getY() - 24);
        } else if (skinWidget != null) {
            this.locatorbar$button.setX(skinWidget.getX());
            this.locatorbar$button.setY(skinWidget.getY() - 24);
        } else if (fovWidget != null) {
            this.locatorbar$button.setX(fovWidget.getX());
            this.locatorbar$button.setY(fovWidget.getY() + 24);
        } else {
            this.locatorbar$button.setX(this.width / 2 - 155);
            this.locatorbar$button.setY(this.height - 28);
        }
    }

    @Unique
    private AbstractWidget locatorbar$findWidgetByKey(String key) {
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
