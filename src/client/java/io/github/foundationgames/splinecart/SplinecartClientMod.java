package io.github.foundationgames.splinecart;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = "splinecart", dist = Dist.CLIENT)
public class SplinecartClientMod {
    private SplinecartClient splinecartClient;

    public SplinecartClientMod(IEventBus bus) {
        this.splinecartClient = new SplinecartClient();

        bus.addListener(this::onClientSetup);
        bus.register(splinecartClient);
    }

    public void onClientSetup(FMLClientSetupEvent ev) {
        this.splinecartClient.onInitializeClient();
    }
}
