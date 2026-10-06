// AUTOTOTEM — держит тотем в левой руке
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
