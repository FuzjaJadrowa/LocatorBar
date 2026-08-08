package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.lang.reflect.InvocationTargetException;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        //? if >=1.21.11 {
        LocatorBar.init(FMLEnvironment.getDist() == Dist.CLIENT);
        //?} else {
        /*LocatorBar.init(FMLEnvironment.dist == Dist.CLIENT);
        *///?}
        modEventBus.addListener(LocatorBarNeoForgeNetworking::registerPayloads);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeNetworking::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeNetworking::onServerTick);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForge::onRegisterCommands);

        LocatorBar.broadcaster = settings -> {
            net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                new pl.fuzjajadrowa.locatorbar.network.ServerConfigPayload(settings)
            );
        };
        //? if >=1.21.11 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            initClient(modContainer);
        }
        //?} else {
        /*if (FMLEnvironment.dist == Dist.CLIENT) {
            initClient(modContainer);
        }
        *///?}
    }

    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(event.getDispatcher());
    }

    private static void initClient(ModContainer modContainer) {
        try {
            Class<?> clientHooks = Class.forName("pl.fuzjajadrowa.locatorbar.neoforge.LocatorBarNeoForgeClient");
            clientHooks.getMethod("init", ModContainer.class).invoke(null, modContainer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Failed to initialize Locator Bar client hooks", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Failed to initialize Locator Bar client hooks", cause);
        }
    }
}