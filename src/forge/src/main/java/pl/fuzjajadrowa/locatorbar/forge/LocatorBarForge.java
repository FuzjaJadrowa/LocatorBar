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

        LocatorBar.setBroadcaster(settings -> {
            LocatorBarForgeNetworking.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                new LocatorBarForgeNetworking.ServerConfigPacket(settings)
            );
        });

        pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory.registerPlatformContainerSupport(
                (stack, consumer) -> {
                    stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                        for (int i = 0; i < handler.getSlots(); i++) {
                            net.minecraft.world.item.ItemStack inner = handler.getStackInSlot(i);
                            if (!inner.isEmpty()) {
                                consumer.accept(inner);
                            }
                        }
                    });
                },
                (stack, updater) -> {
                    var opt = stack.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER);
                    if (!opt.isPresent()) {
                        return false;
                    }
                    var handler = opt.orElseThrow(IllegalStateException::new);
                    boolean modified = false;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        net.minecraft.world.item.ItemStack inner = handler.getStackInSlot(i);
                        if (!inner.isEmpty()) {
                            net.minecraft.world.item.ItemStack copy = inner.copy();
                            if (updater.test(copy)) {
                                if (handler instanceof net.minecraftforge.items.IItemHandlerModifiable modifiable) {
                                    modifiable.setStackInSlot(i, copy);
                                    modified = true;
                                } else if (updater.test(inner)) {
                                    modified = true;
                                }
                            }
                        }
                    }
                    return modified;
                }
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            LocatorBarForgeClient.init();
        }
    }

    public static void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(event.getDispatcher());
    }
}