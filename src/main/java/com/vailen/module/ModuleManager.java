package com.vailen.module;

import com.vailen.gui.Category;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    static {
        // Combat
        MODULES.add(new Module("KillAura", Category.COMBAT));
        MODULES.add(new Module("AutoClicker", Category.COMBAT));

        // Movement
        MODULES.add(new Module("Sprint", Category.MOVEMENT));
        MODULES.add(new Module("Fly", Category.MOVEMENT));

        // Visuals
        MODULES.add(new Module("Fullbright", Category.VISUALS));
        MODULES.add(new Module("NoWeather", Category.VISUALS));
        MODULES.add(new Module("NoParticles", Category.VISUALS));

        // Render
        MODULES.add(new Module("ESP", Category.RENDER));
        MODULES.add(new Module("FPS Display", Category.RENDER));

        // Misc
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
