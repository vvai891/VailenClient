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

    // ========== ЦВЕТА (Meteor-стиль, фиолетовый акцент) ==========
    private static final int PANEL_BG     = 0xE80F0F1A;
    private static final int HEADER_BG    = 0xFF15152A;
    private static final int ACCENT       = 0xFF8B5CF6;
    private static final int MODULE_BG    = 0xAA1A1A26;
    private static final int MODULE_HOVER = 0xCC252535;
    private static final int MODULE_ON    = 0xFF8B5CF6;
    private static final int TEXT         = 0xFFDDDDDD;
    private static final int TEXT_ON      = 0xFFFFFFFF;
    private static final int TEXT_DIM     = 0xFF888888;
    private static final int SHADOW       = 0x66000000;
    private static final int SEARCH_BG    = 0xFF1A1A26;

    private static final int PANEL_W      = 90;
    private static final int HEADER_H     = 16;
    private static final int MODULE_H     = 13;
    private static final int MODULE_GAP   = 1;
    private static final int PANEL_PAD    = 3;
    private static final int SEARCH_H     = 14;

    private final List<Module> MODULES = ModuleManager.getModules();
    private final Map<Category, List<Module>> byCategory = new HashMap<>();

    private static class PanelState {
        Category category;
        int x, y;
        boolean dragging;
        int dragX, dragY;
        PanelState(Category c, int x, int y) { this.category = c; this.x = x; this.y = y; }
    }

    private final List<PanelState> panels = new ArrayList<>();
    private PanelState activePanel = null;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        // Группируем модули по категориям
        byCategory.clear();
        for (Category c : Category.values()) byCategory.put(c, new ArrayList<>());
        for (Module m : MODULES) byCategory.get(m.getCategory()).add(m);

        // Раскладываем панели в ряд
        panels.clear();
        int startX = 6;
        int startY = 6;
        int col = 0;

        for (Category c : Category.values()) {
            int px = startX + col * (PANEL_W + 3);
            panels.add(new PanelState(c, px, startY));
            col++;
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // без блюра
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // затемнение
        ctx.fill(0, 0, this.width, this.height, 0x77000000);

        // Панели
        for (PanelState p : panels) {
            renderPanel(ctx, p, mouseX, mouseY);
        }

        // Поле поиска внизу по центру
        renderSearchBar(ctx);

        // Подсказка внизу
        String hint = "Нажмите здесь";
        int hw = this.textRenderer.getWidth(hint);
        ctx.drawTextWithShadow(this.textRenderer, hint,
                (this.width - hw) / 2, this.height - 28, TEXT_DIM);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, PanelState p, int mouseX, int mouseY) {
        List<Module> list = byCategory.get(p.category);
        int bodyH = PANEL_PAD + list.size() * (MODULE_H + MODULE_GAP);
        int panelH = HEADER_H + bodyH + SEARCH_H + PANEL_PAD + 4;

        int x = p.x, y = p.y, w = PANEL_W;

        // Тень
        ctx.fill(x + 2, y + 2, x + w + 2, y + panelH + 2, SHADOW);

        // Фон
        ctx.fill(x, y, x + w, y + panelH, PANEL_BG);

        // Хедер
        ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);

        // Иконка-квадратик
        ctx.fill(x + 4, y + 5, x + 10, y + 11, ACCENT);

        // Название категории
        ctx.drawTextWithShadow(this.textRenderer, p.category.name,
                x + 14, y + 4, TEXT_ON);

        // Модули
        int my = y + HEADER_H + PANEL_PAD;
        for (Module m : list) {
            int mx = x + PANEL_PAD;
            int mw = w - PANEL_PAD * 2;

            boolean hover = mouseX >= mx && mouseX <= mx + mw
                          && mouseY >= my && mouseY <= my + MODULE_H;

            if (m.isEnabled()) {
                ctx.fill(mx, my, mx + mw, my + MODULE_H, MODULE_ON);
                // Тонкая точка слева
                ctx.fill(mx + 2, my + MODULE_H / 2 - 1,
                         mx + 4, my + MODULE_H / 2 + 1, 0xFFFFFFFF);
                ctx.drawTextWithShadow(this.textRenderer, m.getName(),
                        mx + 6, my + 3, TEXT_ON);
            } else {
                ctx.fill(mx, my, mx + mw, my + MODULE_H,
                        hover ? MODULE_HOVER : MODULE_BG);
                ctx.drawTextWithShadow(this.textRenderer, m.getName(),
                        mx + 6, my + 3, TEXT);
            }

            my += MODULE_H + MODULE_GAP;
        }

        // Поле поиска внутри панели
        int searchY = y + panelH - SEARCH_H - PANEL_PAD;
        int sx = x + PANEL_PAD;
        int sw = w - PANEL_PAD * 2;
        ctx.fill(sx, searchY, sx + sw, searchY + SEARCH_H, SEARCH_BG);
        ctx.drawBorder(sx, searchY, sw, SEARCH_H, 0x33FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "Поиск...",
                sx + 4, searchY + 3, TEXT_DIM);
    }

    private void renderSearchBar(DrawContext ctx) {
        int w = 160;
        int h = 16;
        int x = (this.width - w) / 2;
        int y = this.height - 20;

        ctx.fill(x + 1, y + 1, x + w + 1, y + h + 1, SHADOW);
        ctx.fill(x, y, x + w, y + h, SEARCH_BG);
        ctx.drawBorder(x, y, w, h, ACCENT);

        ctx.drawTextWithShadow(this.textRenderer, "Поиск модулей...",
                x + 5, y + 4, TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Клик по хедеру панели — начать drag
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

            // Клик по модулю
            List<Module> list = byCategory.get(p.category);
            int my2 = p.y + HEADER_H + PANEL_PAD;
            for (Module m : list) {
                int mxx = p.x + PANEL_PAD;
                int mww = PANEL_W - PANEL_PAD * 2;
                if (mx >= mxx && mx <= mxx + mww
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
