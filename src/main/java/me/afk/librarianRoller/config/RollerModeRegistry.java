package me.afk.librarianRoller.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of available roller modes.
 * Owned and registered by {@link ModConfigManager}.
 */
public class RollerModeRegistry {
    private final List<IRollerMode> rollerModes = new ArrayList<>();

    /**
     * Registers the built-in roller modes.
     * Called by {@link ModConfigManager#registerConfig()}.
     */
    public void register() {
        add(new SimpleRollerType.Builder().name("V1").requireCount(1).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V3").requireCount(3).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V4").requireCount(4).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V5").requireCount(5).radius(4D).build());
        add(new SimpleRollerType.Builder().name("V6").requireCount(6).radius(5D).build());
    }

    /**
     * Adds a custom roller mode.
     * Implement {@link IRollerMode} and call this method to register your custom mode.
     */
    public void add(IRollerMode mode) {
        rollerModes.add(mode);
    }

    public List<IRollerMode> getRollerModes() {
        return rollerModes;
    }
}