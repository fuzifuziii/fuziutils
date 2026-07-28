package org.fuzi.fuziutils;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.fuzi.fuziutils.network.FreecamBroadcastPacket;
import org.fuzi.fuziutils.network.FreecamGhostPacket;
import org.fuzi.fuziutils.network.FreecamPosPacket;
import org.fuzi.fuziutils.network.FreecamStatePacket;
import org.fuzi.fuziutils.network.GammaBroadcastPacket;
import org.fuzi.fuziutils.network.GammaStatePacket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FuziUtilsCommon {

    private static final Set<UUID> serverGammaPlayers = new HashSet<>();

    private static final Set<UUID> serverFreecamPlayers = new HashSet<>();

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(FuziUtilsCommon::onRegisterPayloads);
        NeoForge.EVENT_BUS.addListener(FuziUtilsCommon::onPlayerLeave);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var reg = event.registrar("1");

        reg.playToServer(GammaStatePacket.TYPE, GammaStatePacket.CODEC,
                (pkt, ctx) -> {
                    ServerPlayer sender = (ServerPlayer) ctx.player();
                    UUID uuid = sender.getUUID();

                    if (pkt.enabled()) serverGammaPlayers.add(uuid);
                    else               serverGammaPlayers.remove(uuid);

                    GammaBroadcastPacket broadcast = new GammaBroadcastPacket(uuid, pkt.enabled());
                    PacketDistributor.sendToPlayersNear(
                            sender.serverLevel(),
                            sender,
                            sender.getX(), sender.getY(), sender.getZ(),
                            128,
                            broadcast
                    );
                });

        reg.playToClient(GammaBroadcastPacket.TYPE, GammaBroadcastPacket.CODEC,
                (pkt, ctx) -> {
                    if (pkt.enabled()) FuziUtilsClient.gammaPlayers.add(pkt.playerUuid());
                    else               FuziUtilsClient.gammaPlayers.remove(pkt.playerUuid());
                });

        reg.playToServer(FreecamStatePacket.TYPE, FreecamStatePacket.CODEC,
                (pkt, ctx) -> {
                    ServerPlayer sender = (ServerPlayer) ctx.player();
                    UUID uuid = sender.getUUID();

                    sender.setNoGravity(pkt.enabled());
                    if (pkt.enabled()) {
                        serverFreecamPlayers.add(uuid);
                        sender.setForcedPose(net.minecraft.world.entity.Pose.STANDING);
                    } else {
                        serverFreecamPlayers.remove(uuid);
                        sender.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                        sender.setForcedPose(null);
                    }

                    PacketDistributor.sendToPlayersNear(
                            sender.serverLevel(),
                            sender,
                            sender.getX(), sender.getY(), sender.getZ(),
                            128,
                            new FreecamBroadcastPacket(uuid, pkt.enabled())
                    );
                });

        reg.playToClient(FreecamBroadcastPacket.TYPE, FreecamBroadcastPacket.CODEC,
                (pkt, ctx) -> {
                    if (!pkt.enabled()) FuziUtilsClient.ghostPlayers.remove(pkt.playerUuid());
                });

        reg.playToServer(FreecamPosPacket.TYPE, FreecamPosPacket.CODEC,
                (pkt, ctx) -> {
                    ServerPlayer sender = (ServerPlayer) ctx.player();
                    if (!serverFreecamPlayers.contains(sender.getUUID())) return;

                    PacketDistributor.sendToPlayersNear(
                            sender.serverLevel(),
                            sender,
                            pkt.x(), pkt.y(), pkt.z(),
                            128,
                            new FreecamGhostPacket(sender.getUUID(), pkt.x(), pkt.y(), pkt.z(), pkt.yRot(), pkt.xRot())
                    );
                });

        reg.playToClient(FreecamGhostPacket.TYPE, FreecamGhostPacket.CODEC,
                (pkt, ctx) -> FuziUtilsClient.ghostPlayers.put(pkt.playerUuid(), pkt));
    }

    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        if (serverFreecamPlayers.remove(uuid)) {
            PacketDistributor.sendToAllPlayers(new FreecamBroadcastPacket(uuid, false));
        }
        serverGammaPlayers.remove(uuid);

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.setNoGravity(false);
            serverPlayer.setForcedPose(null);
        }
    }
}
