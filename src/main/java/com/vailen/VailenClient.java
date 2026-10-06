package com.vailen;

import com.vailen.gui.ClickGuiScreen;
import com.vailen.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VailenClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;

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

            if (client.player == null) return;

            // SPRINT
            if (ModuleManager.isEnabled("Sprint")) {
                if (client.options.forwardKey.isPressed()
                        && !client.player.isSneaking()
                        && !client.player.isUsingItem()
                        && !client.player.horizontalCollision) {
                    client.player.setSprinting(true);
                }
            }
        });
    }
}
