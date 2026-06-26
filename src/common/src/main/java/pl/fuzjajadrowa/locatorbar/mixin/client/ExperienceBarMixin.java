package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.contextualbar.ExperienceBar;
//?} elif >=1.21.11 {
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.ClassicExperienceBarState;

//? if >=26.2 {
@Mixin(ExperienceBar.class)
//?} elif >=1.21.11 {
@Mixin(ExperienceBarRenderer.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class ExperienceBarMixin {
    //? if >=1.21.11 {
    //? if >=26.1 {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceBarBackground(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?} else {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceBarBackground(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceBar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?}
    //?} else {
    /*@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceBar(GuiGraphicsExtractor guiGraphics, int y, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    *///?}
}