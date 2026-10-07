package com.vailen;

import com.vailen.gui.ClickGuiScreen;
import com.vailen.gui.VisualsScreen;
import com.vailen.hud.HudRenderer;
import com.vailen.hud.StorageESP;
import com.vailen.module.Module;
import com.vailen.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class VailenClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;
    public static KeyBinding openVisualsKey;

    private static double oldGamma = 1.0;
    private static boolean fullbrightActive = false;
    private static ParticlesMode oldParticles = ParticlesMode.ALL;
    private static boolean noParticlesActive = false;

    private static long lastTriggerHit = 0L;
    private static long lastAutoTotem = 0L;
    private static long lastChestSteal = 0L;

    private static final Map<String, Boolean> keyStates = new HashMap<>();

    @Override
    public void onInitializeClient() {
        StorageESP.register();

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

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(".bind")) {
                handleBindCommand(MinecraftClient.getInstance(), message);
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) client.setScreen(new ClickGuiScreen());
            while (openVisualsKey.wasPressed()) client.setScreen(new VisualsScreen());

            if (client.player == null || client.world == null) return;

            for (Module m : ModuleManager.getModules()) {
                if (!m.hasKeybind()) continue;
                boolean pressed = InputUtil.isKeyPressed(
                        client.getWindow().getHandle(), m.getKeybind());
                Boolean was = keyStates.get(m.getName());
                if (pressed && (was == null || !was)) {
                    m.toggle();
                }
                keyStates.put(m.getName(), pressed);
            }

            if (ModuleManager.isEnabled("Sprint")) {
                if (client.options.forwardKey.isPressed()
                        && !client.player.isSneaking()
                        && !client.player.isUsingItem()
                        && !client.player.horizontalCollision) {
                    client.player.setSprinting(true);
                }
            }

            if (ModuleManager.isEnabled("AutoTotem")) {
                long now = System.currentTimeMillis();
                if (now - lastAutoTotem >= 200) {
                    lastAutoTotem = now;
                    boolean offhandHasTotem =
                            client.player.getOffHandStack().isOf(net.minecraft.item.Items.TOTEM_OF_UNDYING);
                    if (!offhandHasTotem) {
                        var inv = client.player.playerScreenHandler;
                        int totemSlot = -1;
                        for (int i = 9; i <= 44; i++) {
                            var stack = inv.getSlot(i).getStack();
                            if (stack.isOf(net.minecraft.item.Items.TOTEM_OF_UNDYING)) {
                                totemSlot = i;
                                break;
                            }
                        }
                        if (totemSlot != -1) {
                            client.interactionManager.clickSlot(
                                    inv.syncId,
                                    totemSlot,
                                    40,
                                    net.minecraft.screen.slot.SlotActionType.SWAP,
                                    client.player
                            );
                        }
                    }
                }
            }

            if (ModuleManager.isEnabled("ChestStealer")) {
                if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> hs) {
                    var handler = hs.getScreenHandler();
                    long now = System.currentTimeMillis();
                    if (now - lastChestSteal >= 60) {
                        lastChestSteal = now;
                        for (int i = 0; i < handler.slots.size(); i++) {
                            var stack = handler.getSlot(i).getStack();
                            if (stack.isEmpty()) continue;
                            if (i >= handler.slots.size() - 36) continue;
                            client.interactionManager.clickSlot(
                                    handler.syncId,
                                    i,
                                    0,
                                    net.minecraft.screen.slot.SlotActionType.QUICK_MOVE,
                                    client.player
                            );
                            break;
                        }
                    }
                }
            }

            if (ModuleManager.isEnabled("AutoAttack")) {
                if (client.crosshairTarget instanceof EntityHitResult ehr) {
                    Entity target = ehr.getEntity();
                    if (target instanceof LivingEntity living && target != client.player) {
                        boolean enemy = (target instanceof PlayerEntity)
                                     || (target instanceof HostileEntity);
                        if (enemy && client.player.getAttackCooldownProgress(0f) >= 1.0f) {
                            client.interactionManager.attackEntity(client.player, living);
                            client.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
            }

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

            if (ModuleManager.isEnabled("NoWeather")) {
                client.world.setRainGradient(0f);
                client.world.setThunderGradient(0f);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            HudRenderer.render(drawContext, tickDelta.getTickDelta(false));
        });
    }

    private static void handleBindCommand(MinecraftClient mc, String message) {
        String[] parts = message.trim().split("\\s+");
        if (parts.length < 2) {
            sendMsg(mc, "§e.bind add <модуль> <клавиша>");
            sendMsg(mc, "§e.bind remove <модуль>");
            sendMsg(mc, "§e.bind list");
            sendMsg(mc, "§e.bind clear");
            return;
        }
        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "add": {
                if (parts.length < 4) { sendMsg(mc, "§c.bind add <модуль> <клавиша>"); return; }
                Module m = findModule(parts[2]);
                if (m == null) { sendMsg(mc, "§cМодуль не найден: §f" + parts[2]); return; }
                int key = keyFromName(parts[3]);
                if (key <= 0) { sendMsg(mc, "§cНеизвестная клавиша: §f" + parts[3]); return; }
                m.setKeybind(key);
                sendMsg(mc, "§aМодуль §f" + m.getName() + " §aпривязан к §d" + m.getKeybindName());
                break;
            }
            case "remove": {
                if (parts.length < 3) { sendMsg(mc, "§c.bind remove <модуль>"); return; }
                Module m = findModule(parts[2]);
                if (m == null) { sendMsg(mc, "§cМодуль не найден"); return; }
                m.setKeybind(-1);
                sendMsg(mc, "§aБинд снят с §f" + m.getName());
                break;
            }
            case "list": {
                boolean any = false;
                for (Module m : ModuleManager.getModules()) {
                    if (m.hasKeybind()) {
                        sendMsg(mc, "§7" + m.getName() + " §f→ §d" + m.getKeybindName());
                        any = true;
                    }
                }
                if (!any) sendMsg(mc, "§7Нет активных биндов");
                break;
            }
            case "clear": {
                for (Module m : ModuleManager.getModules()) m.setKeybind(-1);
                sendMsg(mc, "§aВсе бинды сброшены");
                break;
            }
            default: sendMsg(mc, "§cНеизвестная подкоманда");
        }
    }

    private static Module findModule(String name) {
        for (Module m : ModuleManager.getModules()) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    private static int keyFromName(String name) {
        String n = name.toUpperCase();
        if (n.startsWith("F") && n.length() <= 3) {
            try {
                int num = Integer.parseInt(n.substring(1));
                if (num >= 1 && num <= 25) return GLFW.GLFW_KEY_F1 + (num - 1);
            } catch (Exception ignored) {}
        }
        if (n.length() == 1) {
            char c = n.charAt(0);
            if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }
        switch (n) {
            case "RSHIFT": return GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LSHIFT": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RCTRL":  return GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LCTRL":  return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "SPACE":  return GLFW.GLFW_KEY_SPACE;
            case "TAB":    return GLFW.GLFW_KEY_TAB;
            case "ENTER":  return GLFW.GLFW_KEY_ENTER;
            case "ESC":    return GLFW.GLFW_KEY_ESCAPE;
        }
        return -1;
    }

    private static void sendMsg(MinecraftClient mc, String text) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(text), false);
    }
                }
