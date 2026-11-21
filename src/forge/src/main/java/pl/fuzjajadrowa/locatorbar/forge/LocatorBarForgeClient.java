package pl.fuzjajadrowa.locatorbar.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;
import pl.fuzjajadrowa.locatorbar.client.PlayerLocatorClient;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

public final class LocatorBarForgeClient {
    private LocatorBarForgeClient() {
    }

    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new LocatorBarConfigScreen(parent))
        );
        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeClient::onRenderGui);
        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeClient::onLoggingOut);
        LocatorBar.setUnlinkWaypointSender(waypointId -> {
            if (LocatorBarConfig.hasServerSettings()) {
                LocatorBarForgeNetworking.INSTANCE.sendToServer(new LocatorBarForgeNetworking.UnlinkWaypointPacket(waypointId));
            }
        });
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        LocatorBarHudRenderer.render(event.getGuiGraphics());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LocatorBarConfig.clearServerSettings();
        PlayerLocatorClient.clear();
    }
}