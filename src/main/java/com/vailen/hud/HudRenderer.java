package com.vailen.hud;

import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {

    private static final int ACCENT   = 0xFF7A5CFF;
    private static final int BG       = 0x88101016;
    private static final int TEXT     = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;
    private static final int HP_BG    = 0xFF330000;
    private static final int HP_RED   = 0xFFFF4444;

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (ModuleManager.isEnabled("Watermark"))    renderWatermark(ctx, mc);
        if (ModuleManager.isEnabled("FPS Display"))  renderFps(ctx, mc);
        if (ModuleManager.isEnabled("Ping"))         renderPing(ctx, mc);
        if (ModuleManager.isEnabled("Coordinates"))  renderCoords(ctx, mc);
        if (ModuleManager.isEnabled("ArrayList"))    renderArrayList(ctx, mc);
        if (ModuleManager.isEnabled("TargetHUD"))    renderTargetHud(ctx, mc);
    }

    private static int resolveX(HudElement e, DrawContext ctx) {
        if (e.x < 0) return ctx.getScaledWindowWidth() - e.width - 8;
        return e.x;
    }

    private static void renderWatermark(DrawContext ctx, MinecraftClient mc) {
        HudElement e = HudManager.get("Watermark");
        String text = "VailenClient";
        int w = mc.textRenderer.getWidth(text) + 14;
        int h = 18;
        e.width = w; e.height = h;
        int x = resolveX(e, ctx);
        int y = e.y;
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawBorder(x, y, w, h, ACCENT);
        ctx.drawTextWithShadow(mc.textRenderer, "§l" + text, x + 7, y + 5, ACCENT);
    }

    private static void renderFps(DrawContext ctx, MinecraftClient mc) {
        HudElement e = HudManager.get("FPS Display");
        String text = mc.getCurrentFps() + " FPS";
        int w = mc.textRenderer.getWidth(text) + 12;
        int h = 14;
        e.width = w; e.height = h;
        int x = resolveX(e, ctx);
        int y = e.y;
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, TEXT);
    }

    private static void renderPing(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        HudElement e = HudManager.get("Ping");
        int ping = 0;
        try {
            var entry = mc.getNetworkHandler() != null
                    ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid())
                    : null;
            if (entry != null) ping = entry.getLatency();
        } catch (Exception ignored) {}

        String text = ping + " ms";
        int w = mc.textRenderer.getWidth(text) + 12;
        int h = 14;
        e.width = w; e.height = h;
        int x = resolveX(e, ctx);
        int y = e.y;
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, TEXT_DIM);
    }

    private static void renderCoords(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        HudElement e = HudManager.get("Coordinates");
        String text = String.format("XYZ: %.0f, %.0f, %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int w = mc.textRenderer.getWidth(text) + 12;
        int h = 14;
        e.width = w; e.height = h;
        int x = resolveX(e, ctx);
        int y = e.y;
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, TEXT);
    }

    private static void renderArrayList(DrawContext ctx, MinecraftClient mc) {
        HudElement el = HudManager.get("ArrayList");
        List<Module> enabled = new ArrayList<>();
        for (Module m : ModuleManager.getModules()) {
            if (m.isEnabled() && m.getCategory() != com.vailen.gui.Category.RENDER) {
                enabled.add(m);
            }
        }
        if (enabled.isEmpty()) { el.width = 0; el.height = 0; return; }

        enabled.sort(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        int y = el.y;
        int maxW = 0;
        int startX = el.x;
        List<int[]> positions = new ArrayList<>();

        for (Module m : enabled) {
            String name = m.getName();
            int textW = mc.textRenderer.getWidth(name);
            int w = textW + 14;
            int h = 14;
            int x = (startX < 0) ? ctx.getScaledWindowWidth() - w - 8 : startX;
            positions.add(new int[]{x, y, w, h});
            if (w > maxW) maxW = w;
            y += h + 2;
        }

        for (int i = 0; i < enabled.size(); i++) {
            int[] p = positions.get(i);
            ctx.fill(p[0], p[1], p[0] + p[2], p[1] + p[3], BG);
            ctx.fill(p[0], p[1], p[0] + 2, p[1] + p[3], ACCENT);
            ctx.drawTextWithShadow(mc.textRenderer, enabled.get(i).getName(), p[0] + 8, p[1] + 3, TEXT);
        }

        el.width = maxW;
        el.height = y - el.y;
        if (!positions.isEmpty()) el.x = positions.get(0)[0];
    }

    private static void renderTargetHud(DrawContext ctx, MinecraftClient mc) {
        HudElement el = HudManager.get("TargetHUD");

        Entity target = null;
        if (mc.crosshairTarget instanceof EntityHitResult ehr) {
            target = ehr.getEntity();
        }
        if (!(target instanceof LivingEntity living)) { el.width = 0; el.height = 0; return; }

        String name = living.getName().getString();
        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        float ratio = Math.max(0f, Math.min(1f, hp / maxHp));

        int panelW = 150;
        int panelH = 42;
        int x = el.x;
        int y = el.y;
        if (x < 0) x = (ctx.getScaledWindowWidth() - panelW) / 2;
        if (y < 0) y = ctx.getScaledWindowHeight() / 2 + 40;

        ctx.fill(x + 2, y + 2, x + panelW + 2, y + panelH + 2, 0x44000000);
        ctx.fill(x, y, x + panelW, y + panelH, BG);
        ctx.drawBorder(x, y, panelW, panelH, ACCENT);
        ctx.fill(x, y, x + 3, y + panelH, ACCENT);

        ctx.drawTextWithShadow(mc.textRenderer, name, x + 10, y + 8, TEXT);
        String hpText = String.format("%.1f", hp);
        int hpTextW = mc.textRenderer.getWidth(hpText);
        ctx.drawTextWithShadow(mc.textRenderer, hpText, x + panelW - hpTextW - 10, y + 8, 0xFFFF5555);

        int barX = x + 10;
        int barY = y + 26;
        int barW = panelW - 20;
        int barH = 8;
        ctx.fill(barX, barY, barX + barW, barY + barH, HP_BG);
        ctx.fill(barX, barY, barX + (int)(barW * ratio), barY + barH, HP_RED);
        ctx.drawBorder(barX, barY, barW, barH, ACCENT);

        el.width = panelW;
        el.height = panelH;
        el.x = x;
        el.y = y;
    }
                }
