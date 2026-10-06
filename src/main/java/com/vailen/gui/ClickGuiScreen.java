package com.vailen.gui;

import com.vailen.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    private static final int ACCENT      = 0xFF7A5CFF;
    private static final int PANEL_BG    = 0x99101016;
    private static final int CATEGORY_BG = 0xCC15151C;
    private static final int MODULE_BG   = 0x661A1A22;
    private static final int MODULE_ON   = 0xAA7A5CFF;
    private static final int TEXT        = 0xFFFFFFFF;
    private static final int TEXT_DIM    = 0xFFAAAAAA;

    private static final List<Module> MODULES = new ArrayList<>();
    static {
        MODULES.add(new Module("KillAura", Category.COMBAT));
        MODULES.add(new Module("AutoClicker", Category.COMBAT));
        MODULES.add(new Module("Sprint", Category.MOVEMENT));
        MODULES.add(new Module("Fly", Category.MOVEMENT));
        MODULES.add(new Module("ESP", Category.RENDER));
        MODULES.add(new Module("Fullbright", Category.RENDER));
        MODULES.add(new Module("Timer", Category.MISC));
        MODULES.add(new Module("AntiAFK", Category.MISC));
    }

    private final Map<Category, Integer> scroll = new HashMap<>();
    private Category selectedCategory = Category.COMBAT;
    private boolean dragging = false;
    private int dragX, dragY;
    private int panelX, panelY;
    private final int panelW = 420;
    private final int panelH = 260;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        panelX = (this.width  - panelW) / 2;
        panelY = (this.height - panelH) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        ctx.fill(panelX + 4, panelY + 4, panelX + panelW + 4, panelY + panelH + 4, 0x44000000);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        ctx.drawBorder(panelX, panelY, panelW, panelH, ACCENT);

        ctx.drawTextWithShadow(this.textRenderer, "§lVailenClient", panelX + 12, panelY + 10, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, "v1.0", panelX + panelW - 40, panelY + 10, TEXT_DIM);

        ctx.fill(panelX + 1, panelY + 28, panelX + panelW - 1, panelY + 29, ACCENT);

        int catY = panelY + 40;
        int catW = 100;
        for (Category c : Category.values()) {
            boolean sel = c == selectedCategory;
            int bg = sel ? ACCENT : CATEGORY_BG;
            ctx.fill(panelX + 8, catY, panelX + 8 + catW, catY + 22, bg);
            int color = sel ? 0xFFFFFFFF : TEXT_DIM;
            ctx.drawTextWithShadow(this.textRenderer, c.name, panelX + 16, catY + 7, color);
            catY += 26;
        }

        int modX = panelX + catW + 20;
        int modY = panelY + 40;
        int modW = panelW - catW - 28;
        int sc = scroll.getOrDefault(selectedCategory, 0);

        ctx.enableScissor(modX, modY, modX + modW, panelY + panelH - 8);

        for (Module m : MODULES) {
            if (m.getCategory() != selectedCategory) continue;
            int y = modY + sc;
            if (y + 20 < panelY + 30 || y > panelY + panelH) { modY += 24; continue; }

            boolean hover = mouseX >= modX && mouseX <= modX + modW
                          && mouseY >= y && mouseY <= y + 20;

            int bg = m.isEnabled() ? MODULE_ON : (hover ? 0xAA252530 : MODULE_BG);
            ctx.fill(modX, y, modX + modW, y + 20, bg);
            ctx.drawBorder(modX, y, modW, 20, m.isEnabled() ? ACCENT : 0x33FFFFFF);
            ctx.drawTextWithShadow(this.textRenderer, m.getName(), modX + 8, y + 6, TEXT);

            String state = m.isEnabled() ? "ON" : "OFF";
            int stateColor = m.isEnabled() ? 0xFF7A5CFF : TEXT_DIM;
            ctx.drawTextWithShadow(this.textRenderer, state,
                    modX + modW - this.textRenderer.getWidth(state) - 8, y + 6, stateColor);

            modY += 24;
        }

        ctx.disableScissor();
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= panelX && mx <= panelX + panelW
         && my >= panelY && my <= panelY + 28 && button == 0) {
            dragging = true;
            dragX = (int) mx - panelX;
            dragY = (int) my - panelY;
            return true;
        }

        int catY = panelY + 40;
        int catW = 100;
        for (Category c : Category.values()) {
            if (mx >= panelX + 8 && mx <= panelX + 8 + catW
             && my >= catY && my <= catY + 22) {
                selectedCategory = c;
                return true;
            }
            catY += 26;
        }

        int modX = panelX + catW + 20;
        int modY = panelY + 40;
        int modW = panelW - catW - 28;
        int sc = scroll.getOrDefault(selectedCategory, 0);

        for (Module m : MODULES) {
            if (m.getCategory() != selectedCategory) continue;
            int y = modY + sc;
            if (mx >= modX && mx <= modX + modW && my >= y && my <= y + 20) {
                m.toggle();
                if (m.isEnabled()) m.onEnable(); else m.onDisable();
                return true;
            }
            modY += 24;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            panelX = (int) mx - dragX;
            panelY = (int) my - dragY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll.merge(selectedCategory, (int) (-v * 20), Integer::sum);
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
