package io.github.foundationgames.splinecart;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("splinecart")
public class SplinecartMod {
    private Splinecart splinecart;

    public SplinecartMod(IEventBus bus) {
        this.splinecart = new Splinecart();

        bus.register(splinecart);

        Splinecart.BLOCKS.register(bus);
        Splinecart.ITEMS.register(bus);
        Splinecart.DATA_COMPONENTS.register(bus);
        Splinecart.BLOCK_ENTITY_TYPES.register(bus);
        Splinecart.ENTITY_TYPES.register(bus);
    }
}
