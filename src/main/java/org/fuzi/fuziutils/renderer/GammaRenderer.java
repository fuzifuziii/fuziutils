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

    private static final double RAY_LENGTH = 2.2;
    private static final double EYE_SPACING = 0.2;

    private static final float BEAM_END_RADIUS = 0.1f;
    private static final int BEAM_SEGMENTS = 24;
    private static final float R = 1.0f, G = 0.92f, B = 0.55f;
    private static final float BEAM_START_A = 0.0f, BEAM_END_A = 0.55f;

    private static final float DISC_RADIUS = BEAM_END_RADIUS * 0.85f;
    private static final int DISC_SEGMENTS = 24;
    private static final float CORE_R = 1.0f, CORE_G = 0.9f, CORE_B = 0.6f, CORE_A = 1.0f;

    private static final RenderType RAY_BEAM = RenderType.create(
            "fuziutils_gamma_ray_beam",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_FAN,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    private static final RenderType RAY_DISC = RenderType.create(
            "fuziutils_gamma_ray_disc",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_FAN,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
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

        java.util.List<Vec3[]> rays = new java.util.ArrayList<>();
        for (Player player : mc.level.players()) {
            if (player == mc.player && firstPerson) continue;
            if (!gammaPlayers.contains(player.getUUID())) continue;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 look = player.getViewVector(partialTick);

            Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
            Vec3 right = look.cross(worldUp);
            if (right.lengthSqr() < 1.0E-6) right = new Vec3(1.0, 0.0, 0.0);
            right = right.normalize();
            Vec3 up = right.cross(look).normalize();

            Vec3 eyeOffset = right.scale(EYE_SPACING * 0.5);
            rays.add(new Vec3[]{eyePos.subtract(eyeOffset), look, right, up});
            rays.add(new Vec3[]{eyePos.add(eyeOffset), look, right, up});
        }
        if (rays.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer beamConsumer = bufferSource.getBuffer(RAY_BEAM);
        for (Vec3[] ray : rays) {
            Vec3 origin = ray[0], look = ray[1], right = ray[2], up = ray[3];
            Vec3 rayEnd = origin.add(look.scale(RAY_LENGTH));

            beamConsumer.addVertex(pose, (float) origin.x, (float) origin.y, (float) origin.z)
                    .setColor(R, G, B, BEAM_START_A);
            for (int i = 0; i <= BEAM_SEGMENTS; i++) {
                double angle = 2.0 * Math.PI * i / BEAM_SEGMENTS;
                Vec3 rim = rayEnd.add(right.scale(Math.cos(angle) * BEAM_END_RADIUS))
                        .add(up.scale(Math.sin(angle) * BEAM_END_RADIUS));
                beamConsumer.addVertex(pose, (float) rim.x, (float) rim.y, (float) rim.z)
                        .setColor(R, G, B, BEAM_END_A);
            }
        }

        VertexConsumer discConsumer = bufferSource.getBuffer(RAY_DISC);
        for (Vec3[] ray : rays) {
            Vec3 origin = ray[0], look = ray[1], right = ray[2], up = ray[3];
            Vec3 rayEnd = origin.add(look.scale(RAY_LENGTH));

            discConsumer.addVertex(pose, (float) rayEnd.x, (float) rayEnd.y, (float) rayEnd.z)
                    .setColor(CORE_R, CORE_G, CORE_B, CORE_A);
            for (int i = 0; i <= DISC_SEGMENTS; i++) {
                double angle = 2.0 * Math.PI * i / DISC_SEGMENTS;
                Vec3 rim = rayEnd.add(right.scale(Math.cos(angle) * DISC_RADIUS))
                        .add(up.scale(Math.sin(angle) * DISC_RADIUS));
                discConsumer.addVertex(pose, (float) rim.x, (float) rim.y, (float) rim.z)
                        .setColor(0.0f, 0.0f, 0.0f, 1.0f);
            }
        }

        poseStack.popPose();
    }
}
