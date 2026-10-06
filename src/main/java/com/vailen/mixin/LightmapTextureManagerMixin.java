package com.vailen.mixin;

import com.vailen.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleOption.class)
public class LightmapTextureManagerMixin {

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void vailenclient$forceGamma(CallbackInfoReturnable<Object> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;
        Object self = this;
        Object gamma = mc.options.getGamma();
        if (self == gamma && ModuleManager.isEnabled("Fullbright")) {
            cir.setReturnValue(15.0);
        }
    }
}
