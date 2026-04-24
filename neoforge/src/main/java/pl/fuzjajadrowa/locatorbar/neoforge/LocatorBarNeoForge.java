package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.ReworkedLocatorBarHudRenderer;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LocatorBar.init();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> new LocatorBarConfigScreen(parentScreen));
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        ReworkedLocatorBarHudRenderer.render(event.getGuiGraphics());
    }
}