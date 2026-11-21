package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? if !fabric || <1.21.11 {
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
import pl.fuzjajadrowa.locatorbar.client.ClassicExperienceBarState;

//? if >=1.21.11 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//?}
//? if >=1.21.11 && fabric {
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.Font;
//?}

//? if >=26.2 {
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if !fabric {
import net.minecraft.client.DeltaTracker;
//?}

@Mixin(Hud.class)
//?} else {
/*import net.minecraft.client.gui.Gui;
//? if >=1.20.5 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if !fabric || <1.21.11
import net.minecraft.client.DeltaTracker;
//?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

@Mixin(Gui.class)
*///?}
public abstract class ExperienceLevelMixin {
    //? if >=1.21.11 {
    @ModifyExpressionValue(
            method = "nextContextualInfoState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z")
    )
    private boolean locatorbar$disableVanillaLocatorContextualState(boolean original) {
        return false;
    }
    //?}

    //? if >=26.2 {
    //? if fabric {
    @WrapWithCondition(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private boolean locatorbar$shouldExtractExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        return !ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance());
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
    @WrapWithCondition(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private boolean locatorbar$shouldExtractExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        return !ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance());
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
    @WrapWithCondition(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private boolean locatorbar$shouldRenderExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level) {
        return !ClassicExperienceBarState.shouldHideVanillaExperienceBar(Minecraft.getInstance());
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