package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.fml.common.Mod;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge() {
        LocatorBar.init();
    }
}