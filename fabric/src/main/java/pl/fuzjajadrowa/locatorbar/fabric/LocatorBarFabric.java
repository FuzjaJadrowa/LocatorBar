package pl.fuzjajadrowa.locatorbar.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.ReworkedLocatorBarHudRenderer;

public final class LocatorBarFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        LocatorBar.init();
    }

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> ReworkedLocatorBarHudRenderer.render(guiGraphics));
    }
}