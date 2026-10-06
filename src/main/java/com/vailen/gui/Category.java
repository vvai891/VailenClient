package com.vailen.gui;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    VISUALS("Visuals"),
    RENDER("Render"),
    MISC("Misc");

    public final String name;
    Category(String name) { this.name = name; }
}
