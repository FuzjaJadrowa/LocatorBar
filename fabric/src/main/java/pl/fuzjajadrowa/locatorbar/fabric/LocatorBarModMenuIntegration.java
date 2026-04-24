package pl.fuzjajadrowa.locatorbar.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;

public final class LocatorBarModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LocatorBarConfigScreen::new;
    }
}