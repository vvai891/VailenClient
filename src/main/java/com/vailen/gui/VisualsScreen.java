package com.vailen.gui;

import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class VisualsScreen extends Screen {

    public VisualsScreen() {
        super(Text.literal(""));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int guiWidth = 420;
        int guiHeight = 260;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        context.fill(0, 0, this.width, this.height, 0x99000000);
        context.fill(x - 4, y - 4, x + guiWidth + 4, y + guiHeight + 4, 0x66000000);
        context.fill(x, y, x + guiWidth, y + guiHeight, 0xFF101116);
        context.fill(x, y, x + guiWidth, y + 2, 0xFF5865F2);

        context.drawText(this.textRenderer, "CLIENT", x + 20, y + 18, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, "Visual settings", x + 20, y + 34, 0xFF8B8D98, false);

        drawModule(context, x + 20,  y + 65,  "HUD",         isOn("HUD"));
        drawModule(context, x + 210, y + 65,  "Sprint",      isOn("Sprint"));
        drawModule(context, x + 20,  y + 125, "FullBright",  isOn("Fullbright"));
        drawModule(context, x + 210, y + 125, "NoParticles", isOn("NoParticles"));

        context.drawText(this.textRenderer, "1.21.4", x + 20, y + guiHeight - 25, 0xFF666872, false);
    }

    private boolean isOn(String name) {
        Module m = ModuleManager.get(name);
        return m != null && m.isEnabled();
    }

    private void drawModule(DrawContext context, int x, int y, String name, boolean enabled) {
        int width = 180;
        int height = 45;

        context.fill(x, y, x + width, y + height, enabled ? 0xFF191C2A : 0xFF17181D);
        context.drawText(this.textRenderer, name, x + 12, y + 9, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, enabled ? "ON" : "OFF",
                x + 12, y + 25, enabled ? 0xFF7289DA : 0xFF666872, false);

        int switchX = x + width - 42;
        int switchY = y + 13;

        context.fill(switchX, switchY, switchX + 25, switchY + 15,
                enabled ? 0xFF5865F2 : 0xFF303138);

        int circleX = enabled ? switchX + 17 : switchX + 4;
        context.fill(circleX, switchY + 3, circleX + 9, switchY + 12, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int guiWidth = 420;
        int guiHeight = 260;
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        if (inside(mouseX, mouseY, x + 20,  y + 65, 180, 45)) { toggle("HUD");         return true; }
        if (inside(mouseX, mouseY, x + 210, y + 65, 180, 45)) { toggle("Sprint");      return true; }
        if (inside(mouseX, mouseY, x + 20,  y + 125, 180, 45)) { toggle("Fullbright"); return true; }
        if (inside(mouseX, mouseY, x + 210, y + 125, 180, 45)) { toggle("NoParticles");return true; }

        return true;
    }

    private void toggle(String name) {
        Module m = ModuleManager.get(name);
        if (m != null) m.toggle();
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    }
