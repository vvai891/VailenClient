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
    private static final int ACCENT      = 0xFF8B5CF6;
    private static final int TEXT        = 0xFFCCCCCC;
    private static final int TEXT_ON     = 0xFF4ADE80;
    private static final int TEXT_HEADER = 0xFFFFFFFF;
    private static final int DOT_OFF     = 0xFF2A2A32;
    private static final int DOT_ON      = 0xFF4ADE80;
    private static final int SLIDER_BG   = 0xFF1A1A26;
    private static final int SLIDER_FILL = 0xFF8B5CF6;
    private static final int TOGGLE_ON   = 0xFF4ADE80;
    private static final int TOGGLE_OFF  = 0xFF2A2A32;

    private static final int PANEL_W    = 100;
    private static final int HEADER_H   = 20;
    private static final int MODULE_H   = 12;
    private static final int SLIDER_H   = 14;
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
        int panelW = Math.max(60, Math.min(110, available / count));

        int totalW = count * panelW + totalGap;
        int startX = (this.width - totalW) / 2;

        int maxH = 0;
        for (Category c : Category.values()) {
            int h = calcPanelHeight(byCategory.get(c));
            if (h > maxH) maxH = h;
        }
        int startY = (this.height - maxH) / 2;
        if (startY < 20) startY = 20;

        int col = 0;
        for (Category c : Category.values()) {
            PanelState p = new PanelState(c);
            p.x = startX + col * (panelW + PANEL_GAP);
            p.y = startY;
            p.h = calcPanelHeight(byCategory.get(c));
            panels.add(p);
            col++;
        }
    }

    private int calcPanelHeight(List<Module> list) {
        int h = HEADER_H + PANEL_PAD - 2;
        for (Module m : list) {
            h += MODULE_H;
            if (m.getName().equals("KillAura")) {
                h += SLIDER_H * 3;   // attackRange, aimRange, keepSprint+smartCrits row
            } else if (m.getName().equals("TriggerBot")) {
                h += SLIDER_H;       // delay
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

        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + w, y + h, PANEL_BG);
        ctx.fill(x, y, x + w, y + HEADER_H, HEADER_BG);
        ctx.fill(x, y + HEADER_H - 1, x + w, y + HEADER_H, HEADER_LINE);

        String catName = p.category.name;
        int catW = this.textRenderer.getWidth(catName);
        ctx.drawTextWithShadow(this.textRenderer, catName,
                x + (w - catW) / 2, y + (HEADER_H - 8) / 2, TEXT_HEADER);

        int my = y + HEADER_H + PANEL_PAD - 2;

        for (Module m : list) {
            // Название модуля
            int color = m.isEnabled() ? TEXT_ON : TEXT;
            ctx.drawTextWithShadow(this.textRenderer, m.getName(), x + PANEL_PAD, my + 2, color);

            int dotColor = m.isEnabled() ? DOT_ON : DOT_OFF;
            ctx.fill(x + w - 8, my + MODULE_H / 2 - 1,
                     x + w - 6, my + MODULE_H / 2 + 1, dotColor);

            my += MODULE_H;

            if (m.getName().equals("KillAura")) {
                my = renderSlider(ctx, x, my, w, "Attack", m.getAttackRange(), 1.0f, 6.0f);
                my = renderSlider(ctx, x, my, w, "Aim",    m.getAimRange(),    1.0f, 8.0f);
                my = renderToggleRow(ctx, x, my, w, "KeepSprint", m.isKeepSprint());
                my = renderToggleRow(ctx, x, my, w, "SmartCrits", m.isSmartCrits());
            } else if (m.getName().equals("TriggerBot")) {
                my = renderSlider(ctx, x, my, w, "Delay", m.getDelayMs(), 50f, 500f);
            }

            my += MODULE_GAP;
        }
    }

    private int renderSlider(DrawContext ctx, int px, int y, int w, String label,
                             float value, float min, float max) {
        int sx = px + PANEL_PAD;
        int sw = w - PANEL_PAD * 2;
        int sy = y + 2;

        ctx.fill(sx, sy + 4, sx + sw, sy + 10, SLIDER_BG);

        float ratio = (value - min) / (max - min);
        ratio = Math.max(0f, Math.min(1f, ratio));
        ctx.fill(sx, sy + 4, sx + (int)(sw * ratio), sy + 10, SLIDER_FILL);

        String text = label + " " + String.format("%.1f", value);
        ctx.drawTextWithShadow(this.textRenderer, text,
                sx + 2, sy - 3, 0xFFFFFFFF);

        return y + SLIDER_H;
    }

    private int renderToggleRow(DrawContext ctx, int px, int y, int w, String label, boolean on) {
        int sx = px + PANEL_PAD;
        int sy = y + 2;
        int boxX = px + w - PANEL_PAD - 10;

        ctx.drawTextWithShadow(this.textRenderer, label, sx, sy, TEXT);
        ctx.fill(boxX, sy + 2, boxX + 8, sy + 8, on ? TOGGLE_ON : TOGGLE_OFF);
        if (on) ctx.fill(boxX + 2, sy + 4, boxX + 6, sy + 6, 0xFF000000);

        return y + SLIDER_H;
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
            int my2 = p.y + HEADER_H + PANEL_PAD - 2;

            for (Module m : list) {
                // Клик по модулю
                if (mx >= p.x && mx <= p.x + PANEL_W
                 && my >= my2 && my <= my2 + MODULE_H) {
                    m.toggle();
                    return true;
                }
                my2 += MODULE_H;

                if (m.getName().equals("KillAura")) {
                    int sx = p.x + PANEL_PAD;
                    int sw = PANEL_W - PANEL_PAD * 2;

                    // Attack range
                    if (my >= my2 + 2 && my <= my2 + SLIDER_H) {
                        float r = (float)(mx - sx) / sw;
                        r = clamp(r);
                        m.setAttackRange(1.0f + r * 5.0f);
                        return true;
                    }
                    my2 += SLIDER_H;

                    // Aim range
                    if (my >= my2 + 2 && my <= my2 + SLIDER_H) {
                        float r = (float)(mx - sx) / sw;
                        r = clamp(r);
                        m.setAimRange(1.0f + r * 7.0f);
                        return true;
                    }
                    my2 += SLIDER_H;

                    // KeepSprint
                    if (my >= my2 && my <= my2 + SLIDER_H) {
                        m.setKeepSprint(!m.isKeepSprint());
                        return true;
                    }
                    my2 += SLIDER_H;

                    // SmartCrits
                    if (my >= my2 && my <= my2 + SLIDER_H) {
                        m.setSmartCrits(!m.isSmartCrits());
                        return true;
                    }
                    my2 += SLIDER_H;

                } else if (m.getName().equals("TriggerBot")) {
                    int sx = p.x + PANEL_PAD;
                    int sw = PANEL_W - PANEL_PAD * 2;
                    if (my >= my2 + 2 && my <= my2 + SLIDER_H) {
                        float r = (float)(mx - sx) / sw;
                        r = clamp(r);
                        m.setDelayMs((int)(50 + r * 450));
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

    @Override
    public boolean shouldPause() {
        return false;
    }
            }
