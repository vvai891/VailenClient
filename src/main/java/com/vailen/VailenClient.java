package com.vailen;

import com.vailen.gui.ClickGuiScreen;
import com.vailen.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VailenClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;

    // Fullbright — запоминаем старую гамму, чтобы вернуть при выключении
    private static double oldGamma = 1.0;
    private static boolean fullbrightActive = false;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vailenclient.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.vailenclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }

            if (client.player == null || client.world == null) return;

            // SPRINT
            if (ModuleManager.isEnabled("Sprint")) {
                if (client.options.forwardKey.isPressed()
                        && !client.player.isSneaking()
                        && !client.player.isUsingItem()
                        && !client.player.horizontalCollision) {
                    client.player.setSprinting(true);
                }
            }

            // FULLBRIGHT
            boolean fb = ModuleManager.isEnabled("Fullbright");
            if (fb && !fullbrightActive) {
                oldGamma = client.options.getGamma().getValue();
                fullbrightActive = true;
            }
            if (fb) {
                client.options.getGamma().setValue(100.0);
            } else if (fullbrightActive) {
                client.options.getGamma().setValue(oldGamma);
                fullbrightActive = false;
            }

            // NO PARTICLES
            if (ModuleManager.isEnabled("NoParticles")) {
                client.particleManager.clearParticles();
            }

            // NO WEATHER
            if (ModuleManager.isEnabled("NoWeather")) {
                client.world.setRainGradient(0f);
                client.world.setThunderGradient(0f);
            }
        });

        // HUD — FPS Display
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            if (ModuleManager.isEnabled("FPS Display")) {
                String text = mc.getCurrentFps() + " FPS";
                drawContext.drawTextWithShadow(mc.textRenderer, text, 4, 4, 0xFFFFFFFF);
            }
        });
    }
}
