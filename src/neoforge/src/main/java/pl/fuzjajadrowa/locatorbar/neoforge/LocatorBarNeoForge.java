package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.bus.api.IEventBus;
//? if >=26.1 {
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
//?}
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LocatorBar.init();
        //? if >=26.1 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientHooks.init(modContainer);
        }
        //?} else {
        /*modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> new LocatorBarConfigScreen(parentScreen));
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
        *///?}
    }

    //? if >=26.1 {
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
    //?} else {
    /*private void onRenderGui(RenderGuiEvent.Post event) {
        LocatorBarHudRenderer.render(event.getGuiGraphics());
    }
    *///?}
}
