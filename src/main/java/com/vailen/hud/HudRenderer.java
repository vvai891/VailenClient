package com.vailen.hud;

import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {

    private static final int ACCENT = 0xFF7A5CFF;
    private static final int BG     = 0x88101016;
    private static final int TEXT   = 0xFFFFFFFF;

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (ModuleManager.isEnabled("Watermark"))    renderWatermark(ctx, mc);
        if (ModuleManager.isEnabled("FPS Display"))  renderFps(ctx, mc);
        if (ModuleManager.isEnabled("Coordinates"))  renderCoords(ctx, mc);
        if (ModuleManager.isEnabled("ArrayList"))    renderArrayList(ctx, mc);
    }

    private static void renderWatermark(DrawContext ctx, MinecraftClient mc) {
        String text = "VailenClient";
        int w = mc.textRenderer.getWidth(text) + 12;
        int h = 16;
        int x = 6, y = 6;

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawBorder(x, y, w, h, ACCENT);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 4, ACCENT);
    }

    private static void renderFps(DrawContext ctx, MinecraftClient mc) {
        String text = mc.getCurrentFps() + " FPS";
        int w = mc.textRenderer.getWidth(text) + 10;
        int h = 14;
        int x = 6, y = 6 + 16 + 3;

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 5, y + 3, TEXT);
    }

    private static void renderCoords(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        String text = String.format("XYZ: %.0f, %.0f, %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int w = mc.textRenderer.getWidth(text) + 10;
        int h = 14;
        int x = 6, y = 6 + 16 + 3 + 14 + 3;

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 5, y + 3, TEXT);
    }

    private static void renderArrayList(DrawContext ctx, MinecraftClient mc) {
        List<Module> enabled = new ArrayList<>();
        for (Module m : ModuleManager.getModules()) {
            if (m.isEnabled() && m.getCategory() != com.vailen.gui.Category.RENDER) {
                enabled.add(m);
            }
        }
        if (enabled.isEmpty()) return;

        enabled.sort(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        int y = 6;
        int right = ctx.getScaledWindowWidth() - 6;

        for (Module m : enabled) {
            String name = m.getName();
            int textW = mc.textRenderer.getWidth(name);
            int w = textW + 12;
            int h = 14;
            int x = right - w;

            ctx.fill(x, y, x + w, y + h, BG);
            ctx.fill(x + w - 2, y, x + w, y + h, ACCENT);
            ctx.drawTextWithShadow(mc.textRenderer, name, x + 6, y + 3, TEXT);

            y += h + 2;
        }
    }
}
