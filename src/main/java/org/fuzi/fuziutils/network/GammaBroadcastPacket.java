package org.fuzi.fuziutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record GammaBroadcastPacket(UUID playerUuid, boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GammaBroadcastPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("fuziutils", "gamma_broadcast"));

    public static final StreamCodec<FriendlyByteBuf, GammaBroadcastPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.playerUuid);
                        buf.writeBoolean(pkt.enabled);
                    },
                    buf -> new GammaBroadcastPacket(buf.readUUID(), buf.readBoolean())
            );

    @Override
    public CustomPacketPayload.Type<GammaBroadcastPacket> type() {
        return TYPE;
    }
}
