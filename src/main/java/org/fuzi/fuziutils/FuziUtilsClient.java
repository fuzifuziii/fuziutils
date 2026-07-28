package org.fuzi.fuziutils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.fuzi.fuziutils.network.FreecamGhostPacket;
import org.fuzi.fuziutils.network.FreecamPosPacket;
import org.fuzi.fuziutils.network.FreecamStatePacket;
import org.fuzi.fuziutils.network.GammaStatePacket;
import org.fuzi.fuziutils.renderer.FreecamRenderer;
import org.fuzi.fuziutils.renderer.GammaRenderer;
import org.fuzi.fuziutils.util.GammaOverride;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(value = FuziUtils.MODID, dist = Dist.CLIENT)
public class FuziUtilsClient {

    public static final KeyMapping KEY_FREECAM = new KeyMapping(
            "key.fuziutils.freecam",
            GLFW.GLFW_KEY_C,
            "key.categories.fuziutils"
    );

    public static final KeyMapping KEY_GAMMA = new KeyMapping(
            "key.fuziutils.gamma",
            GLFW.GLFW_KEY_G,
            "key.categories.fuziutils"
    );

    public static boolean freecamActive = false;
    public static boolean gammaActive = false;

    private static final double FULLBRIGHT_GAMMA = 100.0;

    private static final float FREECAM_SPEED = 1.0f;

    public static final Set<UUID> gammaPlayers = new HashSet<>();

    public static final Map<UUID, FreecamGhostPacket> ghostPlayers = new HashMap<>();

    private static FreecamEntity freecamEntity = null;
    private static Vec3 savedPos = null;
    private static float savedYaw = 0, savedPitch = 0;
    private static double savedGamma = 1.0;
    private static CameraType savedCameraType = CameraType.FIRST_PERSON;

    public FuziUtilsClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterKeys);
        modEventBus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onRenderNameTag);
        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onComputeCameraAngles);
        NeoForge.EVENT_BUS.addListener(FuziUtilsClient::onLoggingOut);
    }

    private void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_FREECAM);
        event.register(KEY_GAMMA);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        FuziUtils.LOGGER.info("[FuziUtils] Client setup OK");
    }

    static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        while (KEY_FREECAM.consumeClick()) toggleFreecam(mc);
        while (KEY_GAMMA.consumeClick())   toggleGamma(mc);
    }

    static void toggleFreecam(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        freecamActive = !freecamActive;

        if (freecamActive) {
            savedPos   = player.position();
            savedYaw   = player.getYRot();
            savedPitch = player.getXRot();

            freecamEntity = new FreecamEntity(player.level(), player);
            freecamEntity.setOldPosAndRot();
            player.level().addFreshEntity(freecamEntity);
            mc.setCameraEntity(freecamEntity);

            savedCameraType = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.FIRST_PERSON);

            player.setForcedPose(Pose.STANDING);

            player.sendSystemMessage(Component.translatable("message.fuziutils.freecam.on"));
        } else {
            mc.setCameraEntity(player);
            if (freecamEntity != null) {
                freecamEntity.discard();
                freecamEntity = null;
            }
            player.setYRot(savedYaw);
            player.setXRot(savedPitch);
            player.setForcedPose(null);
            mc.options.setCameraType(savedCameraType);
            player.sendSystemMessage(Component.translatable("message.fuziutils.freecam.off"));
        }

        PacketDistributor.sendToServer(new FreecamStatePacket(freecamActive));
    }

    static void toggleGamma(Minecraft mc) {
        gammaActive = !gammaActive;

        if (gammaActive) {
            savedGamma = mc.options.gamma().get();
            GammaOverride.setRaw(mc.options.gamma(), FULLBRIGHT_GAMMA);
            if (mc.player != null) {
                gammaPlayers.add(mc.player.getUUID());
                mc.player.sendSystemMessage(Component.translatable("message.fuziutils.gamma.on"));
            }
        } else {
            mc.options.gamma().set(savedGamma);
            if (mc.player != null) {
                gammaPlayers.remove(mc.player.getUUID());
                mc.player.sendSystemMessage(Component.translatable("message.fuziutils.gamma.off"));
            }
        }

        PacketDistributor.sendToServer(new GammaStatePacket(gammaActive));
    }

    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (!freecamActive || freecamEntity == null || savedPos == null) return;

        float speed = FREECAM_SPEED;
        Vec3 look  = freecamEntity.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

        Vec3 delta = Vec3.ZERO;
        if (isVanillaKeyDown("key.forward")) delta = delta.add(look.scale(speed));
        if (isVanillaKeyDown("key.back"))    delta = delta.add(look.scale(-speed));
        if (isVanillaKeyDown("key.right"))   delta = delta.add(right.scale(speed));
        if (isVanillaKeyDown("key.left"))    delta = delta.add(right.scale(-speed));
        if (isVanillaKeyDown("key.jump"))    delta = delta.add(0, speed, 0);
        if (isVanillaKeyDown("key.sneak"))   delta = delta.add(0, -speed, 0);

        freecamEntity.setOldPosAndRot();

        freecamEntity.setYRot(player.getYRot());
        freecamEntity.setXRot(player.getXRot());
        freecamEntity.moveCamera(delta);

        player.xBobO = player.xBob;
        player.yBobO = player.yBob;
        player.xBob = player.xBob + (player.getXRot() - player.xBob) * 0.5F;
        player.yBob = player.yBob + (player.getYRot() - player.yBob) * 0.5F;

        Vec3 velocity = player.getDeltaMovement();
        player.setPos(savedPos.x, player.getY(), savedPos.z);
        player.setDeltaMovement(0.0, velocity.y, 0.0);

        Vec3 ghostPos = freecamEntity.getEyePosition();
        PacketDistributor.sendToServer(new FreecamPosPacket(
                ghostPos.x, ghostPos.y, ghostPos.z,
                freecamEntity.getYRot(), freecamEntity.getXRot()
        ));
    }

    static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        boolean needsSelfGhost = freecamActive && freecamEntity != null;
        if (gammaPlayers.isEmpty() && !needsSelfGhost && ghostPlayers.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        if (!gammaPlayers.isEmpty()) {
            GammaRenderer.render(poseStack, bufferSource, gammaPlayers, partialTick);
        }
        if (needsSelfGhost && mc.player != null) {
            FreecamRenderer.renderSelf(poseStack, bufferSource, freecamEntity, mc.player, partialTick);
        }
        if (!ghostPlayers.isEmpty()) {
            FreecamRenderer.renderOthers(poseStack, bufferSource, ghostPlayers, partialTick);
        }

        bufferSource.endBatch();
    }

    static void onRenderNameTag(RenderNameTagEvent event) {
        if (FreecamRenderer.renderingGhost) {
            event.setCanRender(TriState.FALSE);
        }
    }

    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!freecamActive || mc.player == null || !(event.getCamera().getEntity() instanceof FreecamEntity)) return;

        event.setYaw(mc.player.getViewYRot((float) event.getPartialTick()));
        event.setPitch(mc.player.getViewXRot((float) event.getPartialTick()));
    }

    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();

        if (freecamActive) {
            mc.options.setCameraType(savedCameraType);
        }
        freecamActive = false;
        freecamEntity = null;
        savedPos = null;

        if (gammaActive) {
            mc.options.gamma().set(savedGamma);
        }
        gammaActive = false;
        gammaPlayers.clear();
        ghostPlayers.clear();
    }

    private static boolean isVanillaKeyDown(String translationKey) {
        for (KeyMapping km : Minecraft.getInstance().options.keyMappings) {
            if (km.getName().equals(translationKey)) {
                return km.isDown();
            }
        }
        return false;
    }
}
