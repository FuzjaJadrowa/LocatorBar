package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LocatorBar.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHooks.init(modContainer);
        }
    }

    private static final class ClientHooks {
        private ClientHooks() {
        }

        private static void init(ModContainer modContainer) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> new LocatorBarConfigScreen(parentScreen));
            NeoForge.EVENT_BUS.addListener(ClientHooks::onRenderGui);
        }

        private static void onRenderGui(RenderGuiEvent.Post event) {
            LocatorBarHudRenderer.render(event.getGuiGraphics());
        }
    }
}