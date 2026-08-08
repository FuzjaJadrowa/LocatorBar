package pl.fuzjajadrowa.locatorbar.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarForge {
    public LocatorBarForge() {
        LocatorBar.init(FMLEnvironment.dist == Dist.CLIENT);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(LocatorBarForgeNetworking::register);

        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeNetworking::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeNetworking::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(LocatorBarForge::onRegisterCommands);

        LocatorBar.broadcaster = settings -> {
            LocatorBarForgeNetworking.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                new LocatorBarForgeNetworking.ServerConfigPacket(settings)
            );
        };

        if (FMLEnvironment.dist == Dist.CLIENT) {
            LocatorBarForgeClient.init();
        }
    }

    public static void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(event.getDispatcher());
    }
}