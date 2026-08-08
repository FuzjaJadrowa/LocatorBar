package pl.fuzjajadrowa.locatorbar.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

public final class LocatorBarFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LocatorBar.init(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        LocatorBarFabricNetworking.initCommon();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(dispatcher);
        });
    }
}