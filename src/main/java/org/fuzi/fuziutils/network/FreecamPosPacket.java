package org.fuzi.fuziutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FreecamPosPacket(double x, double y, double z, float yRot, float xRot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FreecamPosPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("fuziutils", "freecam_pos"));

    public static final StreamCodec<FriendlyByteBuf, FreecamPosPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeDouble(pkt.x);
                        buf.writeDouble(pkt.y);
                        buf.writeDouble(pkt.z);
                        buf.writeFloat(pkt.yRot);
                        buf.writeFloat(pkt.xRot);
                    },
                    buf -> new FreecamPosPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readFloat(), buf.readFloat())
            );

    @Override
    public CustomPacketPayload.Type<FreecamPosPacket> type() {
        return TYPE;
    }
}
