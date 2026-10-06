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

    // ========== ЦВЕТА Meteor ==========
    private static final int PANEL_BG     = 0xF00F0F1A;
    private static final int HEADER_BG    = 0xFF15152A;
    private static final int ACCENT       = 0xFF8B5CF6;
    private static final int MODULE_BG    = 0xAA1A1A26;
    private static final int MODULE_HOVER = 0xCC252535;
    private static final int MODULE_ON    = 0xFF8B5CF6;
    private static final int TEXT         = 0xFFDDDDDD;
    private static final int TEXT_ON      = 0xFFFFFFFF;
    private static final int TEXT_DIM     = 0xFF888888;
    private static final int SHADOW       = 0x88000000;
    private static final int SEARCH_BG    = 0xFF1A1A26;
    private static final int BORDER       = 0x33FFFFFF;

    // ========== РАЗМЕРЫ (подбираются в init) ==========
    private int PANEL_W;
    private int HEADER_H = 16;
    private int MODULE_H = 13;
    private int MODULE_GAP = 1;
    private int PANEL_PAD = 3;
    private int SEARCH_H = 13;
    private int PANEL_GAP = 4;

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
        int sideMargin = 10;
        int totalGap = (count - 1) * PANEL_GAP;
        int available = this.width - sideMargin * 2 - totalGap;

        // Автоматически подбираем ширину панелей, чтобы всё влезло
        PANEL_W = Math.max(48, available / count);
        if (PANEL_W > 95) PANEL_W = 95;

        // Пересчитываем, чтобы всё было по центру
        int totalW = count * PANEL_W + totalGap;
        int startX = (this.width - totalW) / 2;

        // Находим максимальную высоту для вертикального центрирования
        int maxH = 0;
        for (Category c : Category.values()) {
            int listSize = byCategory.get(c).size();
            int panelH = HEADER_H + PANEL_PAD
                       + listSize * (MODULE_H + MODULE_GAP)
                       + SEARCH_H + PANEL_PAD + 4;
            if (panelH > maxH) maxH = panelH;
        }
        int startY = (this.height - maxH) / 2;

        int col = 0;
        for (Category c : Category.values()) {
            PanelState p = new PanelState(c);
            p.x = startX + col * (PANEL_W + PANEL_GAP);
            p.y = startY;
            int listSize = byCategory.get(c).size();
            p.h = HEADER_H + PANEL_PAD
                + listSize * (MODULE_H + MODULE_GAP)
                + SEARCH_H + PANEL_PAD + 4;
            panels.add(p);
            col++;
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // без блюра
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x77000000);

        for (PanelState p : panels) {
            renderPanel(ctx, p, mouseX, mouseY);
        }

        // Поле поиска внизу по центру
        int sw = 160;
        int sh = 14;
        int sx = (this.width - sw) / 2;
        int sy = this.height - 20;
        ctx.fill(sx, sy, sx + sw, sy + sh, SEARCH_BG);
        ctx.drawBorder(sx, sy, sw, sh, ACCENT);
        ctx.drawTextWithShadow(this.textRenderer, "Поиск...",
                sx + 5, sy + 3, TEXT_DIM);

        // Подсказка
        String hint = "Нажмите здесь";
        int hw = this.textRenderer.getWidth(hint);
        ctx.drawTextWithShadow(this.textRenderer, hint,
                (this.width - hw) / 2, sy + sh + 3, TEXT_DIM);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, PanelState p, int mouseX, int mouseY) {
        List<Module> list = byCategory.get(p.category);
        int x = p.x, y = p.y, w = PANEL_W, h = p.h;

        // Тень
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, SHADOW);

        // Фон
        ctx.fill(x, y, x + w, y + h, PANEL_BG);

        // Хедер
        ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);

        // Квадратик-иконка (как в Meteor)
        ctx.fill(x + 3, y + 5, x + 9, y + 11, ACCENT);

        // Название категории
        ctx.drawTextWithShadow(this.textRenderer, p.category.name,
                x + 12, y + 4, TEXT_ON);

        // Модули
        int my = y + HEADER_H + PANEL_PAD;
        for (Module m : list) {
            int mx = x + PANEL_PAD;
            int mw = w - PANEL_PAD * 2;

            boolean hover = mouseX >= mx && mouseX <= mx + mw
                          && mouseY >= my && mouseY <= my + MODULE_H;

            if (m.isEnabled()) {
                ctx.fill(mx, my, mx + mw, my + MODULE_H, MODULE_ON);
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
        int searchY = y + h - SEARCH_H - PANEL_PAD;
        int sx = x + PANEL_PAD;
        int sw = w - PANEL_PAD * 2;
        ctx.fill(sx, searchY, sx + sw, searchY + SEARCH_H, SEARCH_BG);
        ctx.drawBorder(sx, searchY, sw, SEARCH_H, BORDER);
        ctx.drawTextWithShadow(this.textRenderer, "Поиск...",
                sx + 3, searchY + 3, TEXT_DIM);
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
