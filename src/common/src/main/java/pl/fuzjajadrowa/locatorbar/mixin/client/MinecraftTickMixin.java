package pl.fuzjajadrowa.locatorbar.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fuzjajadrowa.locatorbar.client.ClientWaypointInventory;

@Mixin(Minecraft.class)
public abstract class MinecraftTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void locatorbar$refreshInventory(CallbackInfo ci) {
        ClientWaypointInventory.tick(Minecraft.getInstance().player);
    }
}