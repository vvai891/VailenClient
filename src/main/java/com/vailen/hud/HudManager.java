package com.vailen.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HudManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<HudElement> ELEMENTS = new ArrayList<>();
    private static HudElement activeDrag = null;
    private static int dragOffsetX, dragOffsetY;

    static {
        ELEMENTS.add(new HudElement("Watermark", 8, 8));
        ELEMENTS.add(new HudElement("FPS Display", 8, 30));
        ELEMENTS.add(new HudElement("Ping", 8, 47));
        ELEMENTS.add(new HudElement("Coordinates", 8, 64));
        ELEMENTS.add(new HudElement("ArrayList", -1, 8));
        ELEMENTS.add(new HudElement("TargetHUD", -1, -1));
    }

    public static List<HudElement> getElements() { return ELEMENTS; }

    public static HudElement get(String name) {
        for (HudElement e : ELEMENTS) if (e.name.equals(name)) return e;
        return null;
    }

    public static boolean mouseClicked(double mx, double my) {
        for (HudElement e : ELEMENTS) {
            if (e.width <= 0 || e.height <= 0) continue;
            if (mx >= e.x && mx <= e.x + e.width
             && my >= e.y && my <= e.y + e.height) {
                activeDrag = e;
                dragOffsetX = (int) mx - e.x;
                dragOffsetY = (int) my - e.y;
                return true;
            }
        }
        return false;
    }

    public static boolean mouseDragged(double mx, double my) {
        if (activeDrag != null) {
            activeDrag.x = (int) mx - dragOffsetX;
            activeDrag.y = (int) my - dragOffsetY;
            return true;
        }
        return false;
    }

    public static void mouseReleased() {
        if (activeDrag != null) {
            activeDrag = null;
            save();
        }
    }

    public static void renderEditOverlay(DrawContext ctx) {
        for (HudElement e : ELEMENTS) {
            if (e.width <= 0 || e.height <= 0) continue;
            ctx.drawBorder(e.x, e.y, e.width, e.height, 0xFF7A5CFF);
            ctx.fill(e.x, e.y, e.x + e.width, e.y + 1, 0xFF7A5CFF);
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("vailenclient-hud.json");
    }

    public static void save() {
        try {
            Path p = configPath();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(ELEMENTS));
        } catch (IOException ignored) {}
    }

    public static void load() {
        try {
            Path p = configPath();
            if (!Files.exists(p)) return;
            String json = Files.readString(p);
            HudElement[] arr = GSON.fromJson(json, HudElement[].class);
            if (arr == null) return;
            for (HudElement loaded : arr) {
                HudElement e = get(loaded.name);
                if (e != null) { e.x = loaded.x; e.y = loaded.y; }
            }
        } catch (Exception ignored) {}
    }
}
