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

                drawBox(matrices, vc, box, r, g, b);
            }
        });
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer vc, Box box,
                                float r, float g, float b) {
        var m = matrices.peek().getPositionMatrix();
        float a = 1.0f;

        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        line(vc, m, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(vc, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(vc, m, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(vc, m, x1, y1, z2, x1, y1, z1, r, g, b, a);

        line(vc, m, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(vc, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(vc, m, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(vc, m, x1, y2, z2, x1, y2, z1, r, g, b, a);

        line(vc, m, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(vc, m, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(vc, m, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(vc, m, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static void line(VertexConsumer vc, org.joml.Matrix4f m,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b, float a) {
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(1, 0, 0);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(1, 0, 0);
    }
}
