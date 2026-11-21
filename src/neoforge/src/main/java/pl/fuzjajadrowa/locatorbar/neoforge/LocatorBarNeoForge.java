package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus) {
        //? if >=1.21.11 {
        LocatorBar.init(FMLEnvironment.getDist() == Dist.CLIENT);
        //?} else {
        /*LocatorBar.init(FMLEnvironment.dist == Dist.CLIENT);
        *///?}
        modEventBus.addListener(LocatorBarNeoForgeNetworking::registerPayloads);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeNetworking::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeNetworking::onServerTick);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForge::onRegisterCommands);

        LocatorBar.setBroadcaster(LocatorBarNeoForgeNetworking::broadcastConfig);
    }

    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(event.getDispatcher());
    }
}