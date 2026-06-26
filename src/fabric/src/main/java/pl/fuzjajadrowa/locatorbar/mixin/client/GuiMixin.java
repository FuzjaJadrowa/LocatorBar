package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.DeltaTracker;
//? if <26.2
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class GuiMixin {
    //? if >=26.2 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void locatorbar$renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocatorBarHudRenderer.render(guiGraphics);
    }
    //?} elif >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void locatorbar$renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocatorBarHudRenderer.render(guiGraphics);
    }
    //?} else {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void locatorbar$renderHud(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocatorBarHudRenderer.render(guiGraphics);
    }
    *///?}
}