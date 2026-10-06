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

    private static final int PANEL_BG    = 0xFF0A0A0A;
    private static final int HEADER_BG   = 0xFF1A1A1F;
    private static final int HEADER_LINE = 0xFF2A2A32;
    private static final int TEXT        = 0xFFCCCCCC;
    private static final int TEXT_ON     = 0xFF4ADE80;
    private static final int TEXT_HEADER = 0xFFFFFFFF;
    private static final int DOT_OFF     = 0xFF2A2A32;
    private static final int DOT_ON      = 0xFF4ADE80;
    private static final int SLIDER_BG   = 0xFF1A1A26;
    private static final int SLIDER_FILL = 0xFF8B5CF6;
    private static final int TOGGLE_ON   = 0xFF4ADE80;
    private static final int TOGGLE_OFF  = 0xFF2A2A32;
    private static final int MODE_BG     = 0xFF1A1A26;
    private static final int MODE_SEL    = 0xFF8B5CF6;
    private static final int MODE_TEXT   = 0xFFFFFFFF;
    private static final int SCROLL_BAR  = 0xFF5A5A66;

    private static final int PANEL_W    = 120;
    private static final int HEADER_H   = 20;
    private static final int MODULE_H   = 12;
    private static final int SLIDER_H   = 14;
    private static final int MODE_H     = 14;
    private static final int MODULE_GAP = 1;
    private static final int PANEL_PAD  = 4;
    private static final int PANEL_GAP  = 4;
    private static final int MAX_PANEL_H = 220;

    private final List<Module> MODULES = ModuleManager.getModules();
    private final Map<Category, List<Module>> byCategory = new HashMap<>();

    private static class PanelState {
        Category category;
        int x, y, h;
        int fullH;
        int scroll;
        boolean dragging;
        int dragX, dragY;
        PanelState(Category c) { this.category = c; }
    }

    private final List<PanelState> panels = new ArrayList<>();
    private PanelState activePanel = null;
    private PanelState activeScroll = null;

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
        int panelW = Math.max(80, Math.min(120, available / count));

        int totalW = count * panelW + totalGap;
        int startX = (this.width - totalW) / 2;

        int maxH = 0;
        for (Category c : Category.values()) {
            int full = calcPanelHeight(byCategory.get(c));
            int h = Math.min(full, MAX_PANEL_H);
            if (h > maxH) maxH = h;
        }
        int startY = (this.height - maxH) / 2;
        if (startY < 10) startY = 10;

        int col = 0;
        for (Category c : Category.values()) {
            PanelState p = new PanelState(c);
            p.x = startX + col * (panelW + PANEL_GAP);
            p.y = startY;
            p.fullH = calcPanelHeight(byCategory.get(c));
            p.h = Math.min(p.fullH, MAX_PANEL_H);
            p.scroll = 0;
            panels.add(p);
            col++;
        }
    }

    private int calcPanelHeight(List<Module> list) {
        int h = HEADER_H + PANEL_PAD - 2;
        for (Module m : list) {
            h += MODULE_H;
            if (m.getName().equals("KillAura")) {
                h += MODE_H + 6;
                h += SLIDER_H * 2;
                h += MODE_H + 2;
                h += MODE_H + 2;
                h += SLIDER_H * 3;
            } else if (m.getName().equals("TriggerBot")) {
                h += SLIDER_H;
            }
            h += MODULE_GAP;
        }
        h += PANEL_PAD;
        return h;
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
        boolean needsScroll = p.fullH > h;

        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + w, y + h, PANEL_BG);
        ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);
        ctx.fill(x, y + HEADER_H - 1, x + w, y + HEADER_H, HEADER_LINE);

        String catName = p.category.name;
        int catW = this.textRenderer.getWidth(catName);
        ctx.drawTextWithShadow(this.textRenderer, catName,
                x + (w - catW) / 2, y + (HEADER_H - 8) / 2, TEXT_HEADER);

        // Обрезаем всё что ниже хедера
        ctx.enableScissor(x, y + HEADER_H, x + w, y + h);

        int my = y + HEADER_H + PANEL_PAD - 2 - p.scroll;

        for (Module m : list) {
            if (my + MODULE_H > y + HEADER_H && my < y + h) {
                int color = m.isEnabled() ? TEXT_ON : TEXT;
                ctx.drawTextWithShadow(this.textRenderer, m.getName(), x + PANEL_PAD, my + 2, color);

                int dotColor = m.isEnabled() ? DOT_ON : DOT_OFF;
                ctx.fill(x + w - 8, my + MODULE_H / 2 - 1,
                         x + w - 6, my + MODULE_H / 2 + 1, dotColor);
            }

            my += MODULE_H;

            if (m.getName().equals("KillAura")) {
                my = renderModeButtons(ctx, x, my, w, y + HEADER_H, y + h,
                        new String[]{"FT","ST","RW"}, m.getKillAuraModeName(), m.getKillAuraMode(), 3);
                my = renderSlider(ctx, x, my, w, "Attack", m.getAttackRange(), 1.0f, 6.0f, y + HEADER_H, y + h);
                my = renderSlider(ctx, x, my, w, "Aim",    m.getAimRange(),    1.0f, 8.0f, y + HEADER_H, y + h);
                my = renderModeButtons(ctx, x, my, w, y + HEADER_H, y + h,
                        new String[]{"All","Crts","Corr"}, m.getCritModeName(), m.getCritMode(), 3);
                my = renderModeButtons(ctx, x, my, w, y + HEADER_H, y + h,
                        new String[]{"Free","Focus"}, m.getAimModeName(), m.getAimMode(), 2);
                my = renderToggleRow(ctx, x, my, w, "KeepSprint", m.isKeepSprint(), y + HEADER_H, y + h);
                my = renderToggleRow(ctx, x, my, w, "SmartCrits", m.isSmartCrits(), y + HEADER_H, y + h);
                my = renderToggleRow(ctx, x, my, w, "NoEat",      m.isNoEat(),      y + HEADER_H, y + h);
            } else if (m.getName().equals("TriggerBot")) {
                my = renderSlider(ctx, x, my, w, "Delay", m.getDelayMs(), 50f, 500f, y + HEADER_H, y + h);
            }

            my += MODULE_GAP;
        }

        ctx.disableScissor();

        // Полоса скролла
        if (needsScroll) {
            int barX = x + w - 3;
            int barY = y + HEADER_H + 2;
            int barH = h - HEADER_H - 4;
            int knobH = Math.max(12, (int)(barH * (float) h / p.fullH));
            int maxScroll = p.fullH - h;
            int knobY = barY + (maxScroll > 0 ? (int)((barH - knobH) * ((float) p.scroll / maxScroll)) : 0);

            ctx.fill(barX, barY, barX + 2, barY + barH, 0x33FFFFFF);
            ctx.fill(barX, knobY, barX + 2, knobY + knobH, SCROLL_BAR);
        }
    }

    private int renderModeButtons(DrawContext ctx, int px, int y, int w,
                                  int clipTop, int clipBottom,
                                  String[] labels, String fullName, int selected, int count) {
        int sx = px + PANEL_PAD;
        int sw = w - PANEL_PAD * 2;
        int btnW = (sw - (count - 1)) / count;

        for (int i = 0; i < count; i++) {
            int bx = sx + i * (btnW + 1);
            if (y + MODE_H - 1 < clipTop || y + 1 > clipBottom) continue;
            boolean sel = (selected == i);
            ctx.fill(bx, y + 1, bx + btnW, y + MODE_H - 1, sel ? MODE_SEL : MODE_BG);
            int tw = this.textRenderer.getWidth(labels[i]);
            ctx.drawTextWithShadow(this.textRenderer, labels[i],
                    bx + (btnW - tw) / 2, y + 3, MODE_TEXT);
        }

        if (y + MODE_H + 8 > clipTop && y + MODE_H < clipBottom) {
            ctx.drawTextWithShadow(this.textRenderer, fullName, sx, y + MODE_H + 1, 0xFFAAAAAA);
        }
        return y + MODE_H + 12;
    }

    private int renderSlider(DrawContext ctx, int px, int y, int w, String label,
                             float value, float min, float max, int clipTop, int clipBottom) {
        int sx = px + PANEL_PAD;
        int sw = w - PANEL_PAD * 2;
        int sy = y + 4;

        if (sy + 8 < clipTop || sy - 4 > clipBottom) return y + SLIDER_H;

        ctx.fill(sx, sy + 4, sx + sw, sy + 8, SLIDER_BG);

        float ratio = (value - min) / (max - min);
        ratio = Math.max(0f, Math.min(1f, ratio));
        ctx.fill(sx, sy + 4, sx + (int)(sw * ratio), sy + 8, SLIDER_FILL);

        String text = label + " " + String.format("%.1f", value);
        ctx.drawTextWithShadow(this.textRenderer, text, sx, sy - 4, 0xFFFFFFFF);

        return y + SLIDER_H;
    }

    private int renderToggleRow(DrawContext ctx, int px, int y, int w, String label,
                                boolean on, int clipTop, int clipBottom) {
        int sx = px + PANEL_PAD;
        int boxX = px + w - PANEL_PAD - 10;

        if (y + SLIDER_H < clipTop || y > clipBottom) return y + SLIDER_H;

        ctx.drawTextWithShadow(this.textRenderer, label, sx, y + 2, TEXT);
        ctx.fill(boxX, y + 3, boxX + 8, y + 9, on ? TOGGLE_ON : TOGGLE_OFF);

        return y + SLIDER_H;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            PanelState p = panels.get(i);

            // Клик по хедеру
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

            // Клик мимо панели
            if (mx < p.x || mx > p.x + PANEL_W
             || my < p.y || my > p.y + p.h) continue;

            // Клик по модулям/настройкам
            List<Module> list = byCategory.get(p.category);
            int my2 = p.y + HEADER_H + PANEL_PAD - 2 - p.scroll;

            for (Module m : list) {
                if (mx >= p.x && mx <= p.x + PANEL_W
                 && my >= my2 && my <= my2 + MODULE_H) {
                    m.toggle();
                    return true;
                }
                my2 += MODULE_H;

                if (m.getName().equals("KillAura")) {
                    int sx = p.x + PANEL_PAD;
                    int sw = PANEL_W - PANEL_PAD * 2;

                    if (my >= my2 + 1 && my <= my2 + MODE_H - 1) {
                        int btnW = (sw - 2) / 3;
                        for (int k = 0; k < 3; k++) {
                            int bx = sx + k * (btnW + 1);
                            if (mx >= bx && mx <= bx + btnW) {
                                m.setKillAuraMode(k);
                                return true;
                            }
                        }
                    }
                    my2 += MODE_H + 12;

                    if (my >= my2 + 4 && my <= my2 + SLIDER_H) {
                        m.setAttackRange(1.0f + clamp((float)(mx - sx) / sw) * 5.0f);
                        return true;
                    }
                    my2 += SLIDER_H;

                    if (my >= my2 + 4 && my <= my2 + SLIDER_H) {
                        m.setAimRange(1.0f + clamp((float)(mx - sx) / sw) * 7.0f);
                        return true;
                    }
                    my2 += SLIDER_H;

                    if (my >= my2 + 1 && my <= my2 + MODE_H - 1) {
                        int btnW = (sw - 2) / 3;
                        for (int k = 0; k < 3; k++) {
                            int bx = sx + k * (btnW + 1);
                            if (mx >= bx && mx <= bx + btnW) {
                                m.setCritMode(k);
                                return true;
                            }
                        }
                    }
                    my2 += MODE_H + 12;

                    if (my >= my2 + 1 && my <= my2 + MODE_H - 1) {
                        int btnW = (sw - 1) / 2;
                        for (int k = 0; k < 2; k++) {
                            int bx = sx + k * (btnW + 1);
                            if (mx >= bx && mx <= bx + btnW) {
                                m.setAimMode(k);
                                return true;
                            }
                        }
                    }
                    my2 += MODE_H + 12;

                    if (my >= my2 && my <= my2 + SLIDER_H) { m.setKeepSprint(!m.isKeepSprint()); return true; }
                    my2 += SLIDER_H;
                    if (my >= my2 && my <= my2 + SLIDER_H) { m.setSmartCrits(!m.isSmartCrits()); return true; }
                    my2 += SLIDER_H;
                    if (my >= my2 && my <= my2 + SLIDER_H) { m.setNoEat(!m.isNoEat()); return true; }
                    my2 += SLIDER_H;

                } else if (m.getName().equals("TriggerBot")) {
                    int sx = p.x + PANEL_PAD;
                    int sw = PANEL_W - PANEL_PAD * 2;
                    if (my >= my2 + 4 && my <= my2 + SLIDER_H) {
                        m.setDelayMs((int)(50 + clamp((float)(mx - sx) / sw) * 450));
                        return true;
                    }
                    my2 += SLIDER_H;
                }

                my2 += MODULE_GAP;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private float clamp(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
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

    // === СКРОЛЛ ===
    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        for (PanelState p : panels) {
            if (mx >= p.x && mx <= p.x + PANEL_W
             && my >= p.y && my <= p.y + p.h) {
                int maxScroll = Math.max(0, p.fullH - p.h);
                p.scroll -= (int)(v * 20);
                if (p.scroll < 0) p.scroll = 0;
                if (p.scroll > maxScroll) p.scroll = maxScroll;
                return true;
            }
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
                    }
