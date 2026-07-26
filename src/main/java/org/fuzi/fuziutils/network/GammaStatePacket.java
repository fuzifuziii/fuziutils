package org.fuzi.fuziutils.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GammaStatePacket(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GammaStatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("fuziutils", "gamma_state"));

    public static final StreamCodec<FriendlyByteBuf, GammaStatePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.enabled),
                    buf -> new GammaStatePacket(buf.readBoolean())
            );

    @Override
    public CustomPacketPayload.Type<GammaStatePacket> type() {
        return TYPE;
    }
}
