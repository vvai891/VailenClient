package com.vailen.module;

import com.vailen.gui.Category;

public class Module {
    private final String name;
    private final Category category;
    private boolean enabled;

    private int delayMs = 100;
    private float range = 3.5f;
    private int speed = 10;

    // KillAura specific
    private float attackRange = 3.0f;   // дистанция удара
    private float aimRange = 4.5f;      // дистанция наводки
    private boolean keepSprint = true;  // сохранять спринт
    private boolean smartCrits = true;  // умные криты

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getDelayMs() { return delayMs; }
    public void setDelayMs(int ms) { this.delayMs = ms; }
    public float getRange() { return range; }
    public void setRange(float r) { this.range = r; }
    public int getSpeed() { return speed; }
    public void setSpeed(int s) { this.speed = s; }

    public float getAttackRange() { return attackRange; }
    public void setAttackRange(float v) { this.attackRange = v; }
    public float getAimRange() { return aimRange; }
    public void setAimRange(float v) { this.aimRange = v; }
    public boolean isKeepSprint() { return keepSprint; }
    public void setKeepSprint(boolean v) { this.keepSprint = v; }
    public boolean isSmartCrits() { return smartCrits; }
    public void setSmartCrits(boolean v) { this.smartCrits = v; }

    public boolean hasSlider() {
        return name.equals("KillAura") || name.equals("TriggerBot");
    }

    public void toggle() { enabled = !enabled; }
    public void onEnable() {}
    public void onDisable() {}
}
