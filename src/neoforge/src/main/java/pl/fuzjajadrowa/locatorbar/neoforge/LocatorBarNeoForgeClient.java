package pl.fuzjajadrowa.locatorbar.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;
import pl.fuzjajadrowa.locatorbar.client.PlayerLocatorClient;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.network.UnlinkWaypointPayload;

@Mod(value = LocatorBar.MOD_ID, dist = Dist.CLIENT)
public final class LocatorBarNeoForgeClient {
    public LocatorBarNeoForgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> new LocatorBarConfigScreen(parentScreen));
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeClient::onLoggingOut);
        LocatorBar.setUnlinkWaypointSender(waypointId -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null && connection.hasChannel(UnlinkWaypointPayload.TYPE)) {
                connection.send(new UnlinkWaypointPayload(waypointId));
            }
        });
    }

    private static void onRenderGui(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.TAB_LIST.equals(event.getName())) {
            LocatorBarHudRenderer.render(event.getGuiGraphics());
        }
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LocatorBarConfig.clearServerSettings();
        PlayerLocatorClient.clear();
    }
}