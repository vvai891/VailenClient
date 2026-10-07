package com.vailen.hud;

import com.vailen.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class StorageESP {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!ModuleManager.isEnabled("StorageESP")) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return;

            MatrixStack matrices = context.matrixStack();
            if (matrices == null) return;

            VertexConsumerProvider consumers = context.consumers();
            if (consumers == null) return;

            Vec3d cam = context.camera().getPos();
            VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());

            for (BlockEntity be : mc.world.blockEntities()) {
                if (!(be instanceof ChestBlockEntity)
                 && !(be instanceof EnderChestBlockEntity)
                 && !(be instanceof ShulkerBoxBlockEntity)
                 && !(be instanceof BarrelBlockEntity)
                 && !(be instanceof FurnaceBlockEntity)) continue;

                Box box = new Box(be.getPos()).offset(-cam.x, -cam.y, -cam.z);

                float r, g, b;
                if (be instanceof EnderChestBlockEntity)      { r = 0.7f; g = 0.3f; b = 1.0f; }
                else if (be instanceof FurnaceBlockEntity)   { r = 1.0f; g = 0.6f; b = 0.0f; }
                else                                         { r = 1.0f; g = 0.8f; b = 0.0f; }

                // ИСПРАВЛЕНО: WorldRenderer вместо VertexRendering
                WorldRenderer.drawBox(matrices, vc, box, r, g, b, 1.0f);
            }
        });
    }
}
