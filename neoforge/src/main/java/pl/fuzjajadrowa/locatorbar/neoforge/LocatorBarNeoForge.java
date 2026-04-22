package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge() {
        LocatorBar.init();
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        LocatorBarHudRenderer.render(event.getGuiGraphics());
    }
}