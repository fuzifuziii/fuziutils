package org.fuzi.fuziutils.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.fuzi.fuziutils.FreecamEntity;
import org.fuzi.fuziutils.network.FreecamGhostPacket;

import java.util.Map;
import java.util.UUID;

public class FreecamRenderer {

    private static final float ALPHA = 0.5f;
    private static final float R = 0.6f, G = 0.85f, B = 1.0f;

    public static boolean renderingGhost = false;

    public static void renderSelf(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                   FreecamEntity freecamEntity, LocalPlayer player, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || freecamEntity == null || player == null) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        Vec3 ghostPos = freecamEntity.getEyePosition(partialTick);
        render(poseStack, bufferSource, player, ghostPos, freecamEntity.getYRot(), partialTick);
    }

    public static void renderOthers(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                     Map<UUID, FreecamGhostPacket> ghostPlayers, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ghostPlayers.isEmpty()) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            FreecamGhostPacket ghost = ghostPlayers.get(player.getUUID());
            if (ghost == null) continue;

            render(poseStack, bufferSource, player, new Vec3(ghost.x(), ghost.y(), ghost.z()), ghost.yRot(), partialTick);
        }
    }

    private static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                Player player, Vec3 eyePos, float yRot, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        double feetY = eyePos.y - player.getEyeHeight();

        BlockPos lightPos = BlockPos.containing(eyePos.x, feetY + 1.0, eyePos.z);
        int light = LightTexture.pack(
                mc.level.getBrightness(LightLayer.BLOCK, lightPos),
                mc.level.getBrightness(LightLayer.SKY, lightPos)
        );

        poseStack.pushPose();
        poseStack.translate(eyePos.x - camPos.x, feetY - camPos.y, eyePos.z - camPos.z);

        RenderSystem.setShaderColor(R, G, B, ALPHA);
        renderingGhost = true;
        mc.getEntityRenderDispatcher().render(player, 0.0, 0.0, 0.0,
                yRot, partialTick, poseStack, bufferSource, light);
        renderingGhost = false;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }
}
