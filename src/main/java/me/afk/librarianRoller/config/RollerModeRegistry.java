package me.afk.librarianRoller.utils.villagerAndLectern;

import java.util.ArrayList;
import java.util.List;

public class RollerModeRegistry {
    private static final List<IRollerMode> ROLLER_MODE = new ArrayList<>();

    public static void register() {
        add(new SimpleRollerType.Builder().name("V1").requireCount(1).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V3").requireCount(3).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V4").requireCount(4).radius(2D).build());
        add(new SimpleRollerType.Builder().name("V5").requireCount(5).radius(4D).build());
        add(new SimpleRollerType.Builder().name("V6").requireCount(6).radius(5D).build());
    }

    public static void add(IRollerMode mode) {
        ROLLER_MODE.add(mode);
    }

    public static List<IRollerMode> getRollerModes() {
        return ROLLER_MODE;
    }
}
