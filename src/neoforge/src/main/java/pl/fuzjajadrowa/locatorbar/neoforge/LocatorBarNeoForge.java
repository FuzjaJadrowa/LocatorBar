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
        registerContainerSupport();
    }

    @SuppressWarnings("unchecked")
    private static void registerContainerSupport() {
        try {
            Class<?> capsClass = Class.forName("net.neoforged.neoforge.capabilities.Capabilities$ItemHandler");
            Object itemCap = capsClass.getField("ITEM").get(null);
            java.lang.reflect.Method getCapMethod = net.minecraft.world.item.ItemStack.class.getMethod(
                    "getCapability",
                    Class.forName("net.neoforged.neoforge.capabilities.ItemCapability")
            );
            Class<?> handlerClass = Class.forName("net.neoforged.neoforge.items.IItemHandler");
            java.lang.reflect.Method getSlotsMethod = handlerClass.getMethod("getSlots");
            java.lang.reflect.Method getStackMethod = handlerClass.getMethod("getStackInSlot", int.class);
            Class<?> modifiableClass = Class.forName("net.neoforged.neoforge.items.IItemHandlerModifiable");
            java.lang.reflect.Method setStackMethod = modifiableClass.getMethod("setStackInSlot", int.class, net.minecraft.world.item.ItemStack.class);

            pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory.registerPlatformContainerSupport(
                    (stack, consumer) -> {
                        try {
                            Object handler = getCapMethod.invoke(stack, itemCap);
                            if (handler != null) {
                                int slots = (int) getSlotsMethod.invoke(handler);
                                for (int i = 0; i < slots; i++) {
                                    net.minecraft.world.item.ItemStack inner = (net.minecraft.world.item.ItemStack) getStackMethod.invoke(handler, i);
                                    if (inner != null && !inner.isEmpty()) {
                                        consumer.accept(inner);
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    },
                    (stack, updater) -> {
                        try {
                            Object handler = getCapMethod.invoke(stack, itemCap);
                            if (handler == null) {
                                return false;
                            }
                            int slots = (int) getSlotsMethod.invoke(handler);
                            boolean modified = false;
                            for (int i = 0; i < slots; i++) {
                                net.minecraft.world.item.ItemStack inner = (net.minecraft.world.item.ItemStack) getStackMethod.invoke(handler, i);
                                if (inner != null && !inner.isEmpty()) {
                                    net.minecraft.world.item.ItemStack copy = inner.copy();
                                    if (updater.test(copy)) {
                                        if (modifiableClass.isInstance(handler)) {
                                            setStackMethod.invoke(handler, i, copy);
                                            modified = true;
                                        } else if (updater.test(inner)) {
                                            modified = true;
                                        }
                                    }
                                }
                            }
                            return modified;
                        } catch (Throwable ignored) {
                            return false;
                        }
                    }
            );
        } catch (Throwable ignored) {
        }
    }

    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        pl.fuzjajadrowa.locatorbar.server.LocatorBarCommands.register(event.getDispatcher());
    }
}