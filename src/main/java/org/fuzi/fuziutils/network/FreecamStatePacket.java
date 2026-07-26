package org.fuzi.fuziutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FreecamStatePacket(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FreecamStatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("fuziutils", "freecam_state"));

    public static final StreamCodec<FriendlyByteBuf, FreecamStatePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.enabled),
                    buf -> new FreecamStatePacket(buf.readBoolean())
            );

    @Override
    public CustomPacketPayload.Type<FreecamStatePacket> type() {
        return TYPE;
    }
}
