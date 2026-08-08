package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.ClassicExperienceBarState;

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;

@Mixin(Hud.class)
//?} else {
/*import net.minecraft.client.gui.Gui;
//? if >=1.20.5 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
//?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

@Mixin(Gui.class)
*///?}
public abstract class ExperienceLevelMixin {
    //? if >=26.1 {
    @Inject(method = "extractExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?} else {
    //? if >=1.20.5 {
    /*@Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    *///?} elif >=1.20.2 {
    /*@Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, int y, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    *///?}
    //?}
}