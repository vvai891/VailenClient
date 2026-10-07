package com.vailen.module;

import com.vailen.gui.Category;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    static {
        // Combat
        MODULES.add(new Module("TriggerBot", Category.COMBAT));
        MODULES.add(new Module("AutoAttack", Category.COMBAT));
        MODULES.add(new AutoTotem());

        // Movement
        MODULES.add(new Module("Sprint", Category.MOVEMENT));
        MODULES.add(new Module("Fly", Category.MOVEMENT));

        // Render
        MODULES.add(new Module("HUD", Category.RENDER));
        MODULES.add(new Module("Fullbright", Category.RENDER));
        MODULES.add(new Module("NoWeather", Category.RENDER));
        MODULES.add(new Module("NoParticles", Category.RENDER));
        MODULES.add(new Module("ESP", Category.RENDER));
        MODULES.add(new Module("Nametags", Category.RENDER));
        MODULES.add(new Module("Trajectories", Category.RENDER));
        MODULES.add(new Module("ItemPhysics", Category.RENDER));
        MODULES.add(new Module("SwingAnimation", Category.RENDER));
        MODULES.add(new Module("ArmorHUD", Category.RENDER));
        MODULES.add(new Module("BPS", Category.RENDER));
        MODULES.add(new Module("Keystrokes", Category.RENDER));

        // Misc
        MODULES.add(new Module("ChestStealer", Category.MISC));
        MODULES.add(new Module("AntiAFK", Category.MISC));
        MODULES.add(new Module("Timer", Category.MISC));
    }

    public static List<Module> getModules() { return MODULES; }

    public static boolean isEnabled(String name) {
        for (Module m : MODULES) {
            if (m.getName().equals(name)) return m.isEnabled();
        }
        return false;
    }

    public static Module get(String name) {
        for (Module m : MODULES) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }
}
