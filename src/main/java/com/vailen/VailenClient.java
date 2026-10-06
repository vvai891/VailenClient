// CHESTSTEALER
if (ModuleManager.isEnabled("ChestStealer")) {
    if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> hs) {
        var handler = hs.getScreenHandler();
        long now = System.currentTimeMillis();
        if (now - lastChestSteal >= 60) {
            lastChestSteal = now;
            for (int i = 0; i < handler.slots.size(); i++) {
                var stack = handler.getSlot(i).getStack();
                if (stack.isEmpty()) continue;
                // Только "контейнерные" слоты, не инвентарь игрока
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
