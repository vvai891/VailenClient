package com.vailen.hud;

public class HudElement {
    public String name;
    public int x, y;
    public int width = 80, height = 14;

    public HudElement() {}

    public HudElement(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
}
