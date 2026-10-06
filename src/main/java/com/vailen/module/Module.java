package com.vailen.module;

import com.vailen.gui.Category;

public class Module {
    private final String name;
    private final Category category;
    private boolean enabled;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void toggle() { enabled = !enabled; }
    public void onEnable() {}
    public void onDisable() {}
}
