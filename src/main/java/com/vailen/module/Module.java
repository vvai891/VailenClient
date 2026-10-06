package com.vailen.module;

import com.vailen.gui.Category;
import org.lwjgl.glfw.GLFW;

public class Module {
    private final String name;
    private final Category category;
    private boolean enabled;

    private int delayMs = 100;
    private float range = 3.5f;
    private int speed = 10;

    private float attackRange = 3.0f;
    private float aimRange = 4.5f;
    private boolean keepSprint = true;
    private boolean smartCrits = true;

    private int killAuraMode = 0;
    private int critMode = 0;
    private int aimMode = 0;
    private boolean noEat = true;

    private int keybind = -1;

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

    public int getCritMode() { return critMode; }
    public void setCritMode(int v) { this.critMode = v; }
    public String getCritModeName() {
        switch (critMode) {
            case 0: return "All";
            case 1: return "OnlyCrits";
            case 2: return "Correction";
            default: return "All";
        }
    }

    public int getAimMode() { return aimMode; }
    public void setAimMode(int v) { this.aimMode = v; }
    public String getAimModeName() {
        return aimMode == 0 ? "Free" : "Focus";
    }

    public boolean isNoEat() { return noEat; }
    public void setNoEat(boolean v) { this.noEat = v; }

    public int getKillAuraMode() { return killAuraMode; }
    public void setKillAuraMode(int mode) {
        this.killAuraMode = mode;
        applyKillAuraPreset();
    }

    public String getKillAuraModeName() {
        switch (killAuraMode) {
            case 0: return "FunTime";
            case 1: return "SpookyTime";
            case 2: return "ReallyWorld";
            default: return "FunTime";
        }
    }

    public void applyKillAuraPreset() {
        switch (killAuraMode) {
            case 0:
                attackRange = 3.0f; aimRange = 4.5f; delayMs = 80;
                keepSprint = true; smartCrits = true; critMode = 1; aimMode = 1;
                noEat = true;
                break;
            case 1:
                attackRange = 3.5f; aimRange = 5.5f; delayMs = 100;
                keepSprint = true; smartCrits = true; critMode = 1; aimMode = 1;
                noEat = true;
                break;
            case 2:
                attackRange = 2.8f; aimRange = 3.5f; delayMs = 120;
                keepSprint = false; smartCrits = false; critMode = 0; aimMode = 0;
                noEat = true;
                break;
        }
    }

    public int getKeybind() { return keybind; }
    public void setKeybind(int key) { this.keybind = key; }
    public boolean hasKeybind() { return keybind > 0; }

    public String getKeybindName() {
        if (keybind <= 0) return "";
        if (keybind >= GLFW.GLFW_KEY_F1 && keybind <= GLFW.GLFW_KEY_F25) {
            return "F" + (keybind - GLFW.GLFW_KEY_F1 + 1);
        }
        String name = GLFW.glfwGetKeyName(keybind, 0);
        if (name == null || name.isEmpty()) {
            switch (keybind) {
                case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
                case GLFW.GLFW_KEY_LEFT_SHIFT:  return "LSHIFT";
                case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
                case GLFW.GLFW_KEY_LEFT_CONTROL:  return "LCTRL";
                case GLFW.GLFW_KEY_SPACE:        return "SPACE";
                case GLFW.GLFW_KEY_ENTER:        return "ENTER";
                case GLFW.GLFW_KEY_TAB:          return "TAB";
                case GLFW.GLFW_KEY_ESCAPE:       return "ESC";
                default: return "Key" + keybind;
            }
        }
        return name.toUpperCase();
    }

    public boolean hasSlider() {
        return name.equals("KillAura") || name.equals("TriggerBot");
    }

    public void toggle() { enabled = !enabled; }
    public void onEnable() {}
    public void onDisable() {}
}
