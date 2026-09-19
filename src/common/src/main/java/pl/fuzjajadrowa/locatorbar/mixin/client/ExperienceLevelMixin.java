package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.ClassicExperienceBarState;

//? if >=1.21.11 {
//? if <26.2 {
//? if fabric {
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import org.spongepowered.asm.mixin.injection.Redirect;
//?}
//?}
//?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if fabric {
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.injection.Redirect;
//?} else {
import net.minecraft.client.DeltaTracker;
//?}

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
    //? if >=26.2 {
    //? if fabric {
    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void locatorbar$redirectExtractExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        if (!ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ContextualBar.extractExperienceLevel(guiGraphics, font, level);
        }
    }
    //?} else {
    @Inject(method = "extractExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?}
    //?} elif >=26.1 {
    //? if fabric {
    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void locatorbar$redirectExtractExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        if (!ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ContextualBarRenderer.extractExperienceLevel(guiGraphics, font, level);
        }
    }
    //?} else {
    /*@Inject(method = "extractExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    *///?}
    //?} elif >=1.21.11 {
    //? if fabric {
    // Vanilla renders the level through ContextualBarRenderer; NeoForge adds a separate Gui method.
    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void locatorbar$redirectRenderExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        if (!ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ContextualBarRenderer.renderExperienceLevel(guiGraphics, font, level);
        }
    }
    //?} else {
    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void locatorbar$hideExperienceLevel(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?}
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
