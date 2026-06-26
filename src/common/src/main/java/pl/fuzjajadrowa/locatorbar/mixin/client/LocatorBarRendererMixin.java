package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2
import net.minecraft.client.gui.contextualbar.LocatorBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
//? if >=26.2 {
@Mixin(LocatorBar.class)
//?} else {
/*@Mixin(targets = "net.minecraft.client.gui.contextualbar.LocatorBarRenderer")
*///?}
public abstract class LocatorBarRendererMixin {
    //? if >=26.1 {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideVanillaLocatorBarBackground(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideVanillaLocatorBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }
    //?} else {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideVanillaLocatorBarBackground(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideVanillaLocatorBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }
    //?}
}