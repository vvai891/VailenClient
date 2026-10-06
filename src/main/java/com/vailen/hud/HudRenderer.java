package com.vailen.hud;

import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {

    private static final int ACCENT   = 0xFF7A5CFF;
    private static final int BG       = 0xB0101016;
    private static final int BG_DARK  = 0xE0101016;
    private static final int TEXT     = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;
    private static final int HP_RED   = 0xFFFF4444;

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (!ModuleManager.isEnabled("HUD")) return;

        renderWatermark(ctx, mc);
        renderArrayList(ctx, mc);
        renderTargetHud(ctx, mc);
        renderPotions(ctx, mc);
        renderPlayerInfo(ctx, mc);
        renderCoords(ctx, mc);
    }

    // ========== WATERMARK ==========
    private static void renderWatermark(DrawContext ctx, MinecraftClient mc) {
        String name = "§lVailenClient";
        String fps = mc.getCurrentFps() + " FPS";
        String time = getTime();
        String ping = getPing(mc) + " ms";
        String tps = "20.0 TPS";

        int pad = 6;
        int h = 16;
        int x = 6, y = 6;
        int w = mc.textRenderer.getWidth(name) + mc.textRenderer.getWidth(fps)
              + mc.textRenderer.getWidth(time) + mc.textRenderer.getWidth(ping)
              + mc.textRenderer.getWidth(tps) + pad * 12;

        ctx.fill(x, y, x + w, y + h, BG_DARK);
        ctx.fill(x, y, x + w, y + 1, ACCENT);

        int cx = x + pad;
        ctx.drawTextWithShadow(mc.textRenderer, name, cx, y + 4, ACCENT);
        cx += mc.textRenderer.getWidth(name) + pad * 2;

        ctx.drawTextWithShadow(mc.textRenderer, fps, cx, y + 4, TEXT);
        cx += mc.textRenderer.getWidth(fps) + pad * 2;

        ctx.drawTextWithShadow(mc.textRenderer, time, cx, y + 4, TEXT_DIM);
        cx += mc.textRenderer.getWidth(time) + pad * 2;

        ctx.drawTextWithShadow(mc.textRenderer, ping, cx, y + 4, TEXT);
        cx += mc.textRenderer.getWidth(ping) + pad * 2;

        ctx.drawTextWithShadow(mc.textRenderer, tps, cx, y + 4, TEXT_DIM);
    }

    // ========== ARRAYLIST ==========
    private static void renderArrayList(DrawContext ctx, MinecraftClient mc) {
        List<Module> enabled = new ArrayList<>();
        for (Module m : ModuleManager.getModules()) {
            if (m.isEnabled()
                    && m.getCategory() != com.vailen.gui.Category.RENDER
                    && m.getCategory() != com.vailen.gui.Category.VISUALS) {
                enabled.add(m);
            }
        }
        if (enabled.isEmpty()) return;

        enabled.sort(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        int y = 6 + 16 + 4;
        int x = 6;

        for (Module m : enabled) {
            String text = m.getName();
            int w = mc.textRenderer.getWidth(text) + 12;
            int h = 14;

            ctx.fill(x, y, x + w, y + h, BG);
            ctx.fill(x, y, x + 2, y + h, ACCENT);
            ctx.drawTextWithShadow(mc.textRenderer, text, x + 7, y + 3, TEXT);

            y += h + 2;
        }
    }

    // ========== TARGET HUD ==========
    private static void renderTargetHud(DrawContext ctx, MinecraftClient mc) {
        Entity target = null;
        if (mc.crosshairTarget instanceof EntityHitResult ehr) {
            target = ehr.getEntity();
        }
        if (!(target instanceof LivingEntity living)) return;

        String name = living.getName().getString();
        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        int hearts = (int) Math.ceil(hp / 2.0);
        int maxHearts = (int) Math.ceil(maxHp / 2.0);
        if (maxHearts > 10) maxHearts = 10;
        if (hearts > 10) hearts = 10;

        int panelW = 130;
        int panelH = 30;
        int x = (ctx.getScaledWindowWidth() - panelW) / 2;
        int y = 30;

        ctx.fill(x + 2, y + 2, x + panelW + 2, y + panelH + 2, 0x44000000);
        ctx.fill(x, y, x + panelW, y + panelH, BG_DARK);
        ctx.drawBorder(x, y, panelW, panelH, 0xFF303030);

        ctx.drawTextWithShadow(mc.textRenderer, name, x + 6, y + 5, TEXT);

        String hpText = String.format("%.1f", hp);
        int hpW = mc.textRenderer.getWidth(hpText);
        ctx.drawTextWithShadow(mc.textRenderer, hpText, x + panelW - hpW - 6, y + 5, HP_RED);

        int heartY = y + 17;
        int heartX = x + 6;
        for (int i = 0; i < maxHearts; i++) {
            int color = (i < hearts) ? 0xFFFF4444 : 0xFF3A1010;
            ctx.fill(heartX, heartY, heartX + 8, heartY + 7, color);
            heartX += 9;
        }
    }

    // ========== POTIONS ==========
    private static void renderPotions(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        List<StatusEffectInstance> list = new ArrayList<>(effects);

        int panelW = 130;
        int lineH = 14;
        int panelH = 16 + list.size() * (lineH + 1) + 4;
        int x = ctx.getScaledWindowWidth() - panelW - 6;
        int y = 6;

        ctx.fill(x, y, x + panelW, y + panelH, BG_DARK);
        ctx.fill(x, y, x + 2, y + panelH, ACCENT);
        ctx.drawTextWithShadow(mc.textRenderer, "Active potions", x + 8, y + 4, TEXT);

        int cy = y + 16;
        for (StatusEffectInstance e : list) {
            String name = e.getEffectType().value().getName().getString();
            int seconds = e.getDuration() / 20;
            String dur = String.format("%d:%02d", seconds / 60, seconds % 60);

            ctx.drawTextWithShadow(mc.textRenderer, name, x + 8, cy, TEXT_DIM);
            int dw = mc.textRenderer.getWidth(dur);
            ctx.drawTextWithShadow(mc.textRenderer, dur, x + panelW - dw - 6, cy, TEXT);
            cy += lineH + 1;
        }
    }

    // ========== PLAYER INFO ==========
    private static void renderPlayerInfo(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;

        int panelW = 140;
        int panelH = 56;
        int x = ctx.getScaledWindowWidth() - panelW - 6;
        int y = 6;

        if (!mc.player.getStatusEffects().isEmpty()) {
            y += 16 + mc.player.getStatusEffects().size() * 15 + 8;
        }

        ctx.fill(x, y, x + panelW, y + panelH, BG_DARK);
        ctx.fill(x, y, x + 2, y + panelH, ACCENT);
        ctx.drawTextWithShadow(mc.textRenderer, "PlayerInfo", x + 8, y + 4, ACCENT);

        String nick = "Nick: " + mc.player.getName().getString();
        String ping = "Ping: " + getPing(mc) + " ms";
        String server = "Server: ";
        try {
            if (mc.getCurrentServerEntry() != null) server += mc.getCurrentServerEntry().address;
            else server += "Singleplayer";
        } catch (Exception ignored) { server += "Unknown"; }

        ctx.drawTextWithShadow(mc.textRenderer, nick, x + 8, y + 18, TEXT);
        ctx.drawTextWithShadow(mc.textRenderer, ping, x + 8, y + 30, TEXT_DIM);
        ctx.drawTextWithShadow(mc.textRenderer, server, x + 8, y + 42, TEXT_DIM);
    }

    // ========== COORDINATES ==========
    private static void renderCoords(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        String text = String.format("XYZ: %.0f, %.0f, %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int w = mc.textRenderer.getWidth(text) + 12;
        int h = 14;
        int x = 6;
        int y = 6 + 16 + 4 + 14 * 3 + 6;

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, TEXT);
    }

    // ========== Утилиты ==========
    private static String getTime() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        return String.format("%02d:%02d:%02d",
                c.get(java.util.Calendar.HOUR_OF_DAY),
                c.get(java.util.Calendar.MINUTE),
                c.get(java.util.Calendar.SECOND));
    }

    private static int getPing(MinecraftClient mc) {
        try {
            var entry = mc.getNetworkHandler() != null && mc.player != null
                    ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid())
                    : null;
            if (entry != null) return entry.getLatency();
        } catch (Exception ignored) {}
        return 0;
    }
                 }
