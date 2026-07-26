package org.fuzi.fuziutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record FreecamGhostPacket(UUID playerUuid, double x, double y, double z, float yRot, float xRot)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FreecamGhostPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("fuziutils", "freecam_ghost"));

    public static final StreamCodec<FriendlyByteBuf, FreecamGhostPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUUID(pkt.playerUuid);
                        buf.writeDouble(pkt.x);
                        buf.writeDouble(pkt.y);
                        buf.writeDouble(pkt.z);
                        buf.writeFloat(pkt.yRot);
                        buf.writeFloat(pkt.xRot);
                    },
                    buf -> new FreecamGhostPacket(buf.readUUID(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readFloat(), buf.readFloat())
            );

    @Override
    public CustomPacketPayload.Type<FreecamGhostPacket> type() {
        return TYPE;
    }
}
