package org.fuzi.fuziutils.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

public class GammaRenderer {

    private static final double RAY_LENGTH = 3.0;
    private static final float R = 1.0f, G = 0.92f, B = 0.55f, A = 0.9f;

    private static final float LINE_WIDTH = 12.0f;
    private static final RenderType RAY_LINE = RenderType.create(
            "fuziutils_gamma_ray",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(LINE_WIDTH)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                               Set<UUID> gammaPlayers, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || gammaPlayers.isEmpty()) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();

        VertexConsumer consumer = bufferSource.getBuffer(RAY_LINE);

        for (Player player : mc.level.players()) {
            if (player == mc.player && firstPerson) continue;
            if (!gammaPlayers.contains(player.getUUID())) continue;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 look = player.getViewVector(partialTick);
            Vec3 rayEnd = eyePos.add(look.scale(RAY_LENGTH));

            poseStack.pushPose();
            poseStack.translate(eyePos.x - camPos.x, eyePos.y - camPos.y, eyePos.z - camPos.z);
            PoseStack.Pose pose = poseStack.last();

            float nx = (float) look.x, ny = (float) look.y, nz = (float) look.z;
            consumer.addVertex(pose, 0f, 0f, 0f).setColor(R, G, B, A).setNormal(pose, nx, ny, nz);
            consumer.addVertex(pose, (float) (rayEnd.x - eyePos.x), (float) (rayEnd.y - eyePos.y), (float) (rayEnd.z - eyePos.z))
                    .setColor(R, G, B, A).setNormal(pose, nx, ny, nz);

            poseStack.popPose();
        }
    }
}
