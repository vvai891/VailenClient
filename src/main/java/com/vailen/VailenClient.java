package com.vailen;

import com.vailen.gui.ClickGuiScreen;
import com.vailen.gui.VisualsScreen;
import com.vailen.hud.HudRenderer;
import com.vailen.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class VailenClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;      // Right Shift → ClickGui
    public static KeyBinding openVisualsKey;  // G → VisualsScreen

    private static double oldGamma = 1.0;
    private static boolean fullbrightActive = false;
    private static ParticlesMode oldParticles = ParticlesMode.ALL;
    private static boolean noParticlesActive = false;

    private static long lastTriggerHit = 0L;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vailenclient.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.vailenclient"
        ));

        openVisualsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vailenclient.visuals",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.vailenclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
            while (openVisualsKey.wasPressed()) {
                client.setScreen(new VisualsScreen());
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

            // TRIGGERBOT
            if (ModuleManager.isEnabled("TriggerBot")) {
                if (client.crosshairTarget instanceof EntityHitResult ehr) {
                    Entity target = ehr.getEntity();
                    if (target instanceof LivingEntity living && target != client.player) {
                        boolean isEnemy = (target instanceof PlayerEntity)
                                       || (target instanceof HostileEntity);
                        if (isEnemy && client.player.getAttackCooldownProgress(0f) >= 1.0f) {
                            long delay = ModuleManager.get("TriggerBot").getDelayMs();
                            long now = System.currentTimeMillis();
                            if (now - lastTriggerHit >= delay) {
                                client.interactionManager.attackEntity(client.player, living);
                                client.player.swingHand(Hand.MAIN_HAND);
                                lastTriggerHit = now;
                            }
                        }
                    }
                }
            }

            // FULLBRIGHT
            boolean fb = ModuleManager.isEnabled("Fullbright");
            if (fb && !fullbrightActive) {
                oldGamma = client.options.getGamma().getValue();
                fullbrightActive = true;
            }
            if (fb) client.options.getGamma().setValue(15.0);
            else if (fullbrightActive) {
                client.options.getGamma().setValue(oldGamma);
                fullbrightActive = false;
            }

            // NO PARTICLES
            boolean np = ModuleManager.isEnabled("NoParticles");
            if (np && !noParticlesActive) {
                oldParticles = client.options.getParticles().getValue();
                noParticlesActive = true;
            }
            if (np) client.options.getParticles().setValue(ParticlesMode.MINIMAL);
            else if (noParticlesActive) {
                client.options.getParticles().setValue(oldParticles);
                noParticlesActive = false;
            }

            // NO WEATHER
            if (ModuleManager.isEnabled("NoWeather")) {
                client.world.setRainGradient(0f);
                client.world.setThunderGradient(0f);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            HudRenderer.render(drawContext, tickDelta.getTickDelta(false));
        });
    }
}
