package com.vailen.gui;

import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    // ===== Цвета (как на фото) =====
    private static final int PANEL_BG     = 0xFF0A0A0A;
    private static final int PANEL_BG2    = 0xFF131318;
    private static final int HEADER_BG    = 0xFF1A1A1F;
    private static final int HEADER_LINE  = 0xFF2A2A32;
    private static final int TEXT         = 0xFFCCCCCC;
    private static final int TEXT_ON      = 0xFF4ADE80; // зелёный для включённых
    private static final int TEXT_HEADER  = 0xFFFFFFFF;
    private static final int TEXT_DIM     = 0xFF666666;
    private static final int DOT_OFF      = 0xFF2A2A32;
    private static final int DOT_ON       = 0xFF4ADE80;

    private static final int PANEL_W    = 88;
    private static final int HEADER_H   = 20;
    private static final int MODULE_H   = 12;
    private static final int MODULE_GAP = 1;
    private static final int PANEL_PAD  = 4;
    private static final int PANEL_GAP  = 4;

    private final List<Module> MODULES = ModuleManager.getModules();
    private final Map<Category, List<Module>> byCategory = new HashMap<>();

    private static class PanelState {
        Category category;
        int x, y, h;
        boolean dragging;
        int dragX, dragY;
        PanelState(Category c) { this.category = c; }
    }

    private final List<PanelState> panels = new ArrayList<>();
    private PanelState activePanel = null;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        byCategory.clear();
        for (Category c : Category.values()) byCategory.put(c, new ArrayList<>());
        for (Module m : MODULES) byCategory.get(m.getCategory()).add(m);

        panels.clear();

        int count = Category.values().length;
        int sideMargin = 6;
        int totalGap = (count - 1) * PANEL_GAP;
        int available = this.width - sideMargin * 2 - totalGap;

        // Автоподбор ширины
        int panelW = Math.max(50, Math.min(95, available / count));

        int totalW = count * panelW + totalGap;
        int startX = (this.width - totalW) / 2;

        // Максимальная высота для центрирования
        int maxH = 0;
        for (Category c : Category.values()) {
            int listSize = byCategory.get(c).size();
            int panelH = HEADER_H + PANEL_PAD
                       + listSize * (MODULE_H + MODULE_GAP)
                       + PANEL_PAD;
            if (panelH > maxH) maxH = panelH;
        }
        int startY = (this.height - maxH) / 2;
        if (startY < 20) startY = 20;

        int col = 0;
        for (Category c : Category.values()) {
            PanelState p = new PanelState(c);
            p.x = startX + col * (panelW + PANEL_GAP);
            p.y = startY;
            int listSize = byCategory.get(c).size();
            p.h = HEADER_H + PANEL_PAD
                + listSize * (MODULE_H + MODULE_GAP)
                + PANEL_PAD;
            panels.add(p);
            col++;
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) { }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x66000000);

        for (PanelState p : panels) {
            renderPanel(ctx, p, mouseX, mouseY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, PanelState p, int mouseX, int mouseY) {
        List<Module> list = byCategory.get(p.category);
        int x = p.x, y = p.y, w = PANEL_W, h = p.h;

        // Внешний контур (тень)
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);

        // Фон панели
        ctx.fill(x, y, x + w, y + h, PANEL_BG);

        // Верхняя часть (заголовок)
        ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);

        // Тонкая полоска под хедером
        ctx.fill(x, y + HEADER_H - 1, x + w, y + HEADER_H, HEADER_LINE);

        // Название категории — по центру хедера
        String catName = p.category.name;
        int catW = this.textRenderer.getWidth(catName);
        ctx.drawTextWithShadow(this.textRenderer, catName,
                x + (w - catW) / 2, y + (HEADER_H - 8) / 2, TEXT_HEADER);

        // Модули
        int my = y + HEADER_H + PANEL_PAD - 2;
        for (Module m : list) {
            int mx = x + PANEL_PAD;
            int mw = w - PANEL_PAD * 2;

            boolean hover = mouseX >= x && mouseX <= x + w
                          && mouseY >= my && mouseY <= my + MODULE_H;

            int color = m.isEnabled() ? TEXT_ON
                      : (hover ? 0xFFFFFFFF : TEXT);
            ctx.drawTextWithShadow(this.textRenderer, m.getName(),
                    mx, my + 2, color);

            // Маленькая точка справа от модуля
            int dotColor = m.isEnabled() ? DOT_ON : DOT_OFF;
            ctx.fill(x + w - 8, my + MODULE_H / 2 - 1,
                     x + w - 6, my + MODULE_H / 2 + 1, dotColor);

            my += MODULE_H + MODULE_GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            PanelState p = panels.get(i);
            if (mx >= p.x && mx <= p.x + PANEL_W
             && my >= p.y && my <= p.y + HEADER_H) {
                p.dragging = true;
                p.dragX = (int) mx - p.x;
                p.dragY = (int) my - p.y;
                activePanel = p;
                panels.remove(p);
                panels.add(p);
                return true;
            }

            // Клик по модулю — на всю ширину панели
            List<Module> list = byCategory.get(p.category);
            int my2 = p.y + HEADER_H + PANEL_PAD - 2;
            for (Module m : list) {
                if (mx >= p.x && mx <= p.x + PANEL_W
                 && my >= my2 && my <= my2 + MODULE_H) {
                    m.toggle();
                    return true;
                }
                my2 += MODULE_H + MODULE_GAP;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (activePanel != null && activePanel.dragging) {
            activePanel.x = (int) mx - activePanel.dragX;
            activePanel.y = (int) my - activePanel.dragY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (activePanel != null) {
            activePanel.dragging = false;
            activePanel = null;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
