package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record GrappleUpdatePacket(UUID playerUUID, double x, double y, double z) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GrappleUpdatePacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("grapple_update"));

    public static final StreamCodec<ByteBuf, GrappleUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            GrappleUpdatePacket::playerUUID,
            ByteBufCodecs.DOUBLE,
            GrappleUpdatePacket::x,
            ByteBufCodecs.DOUBLE,
            GrappleUpdatePacket::y,
            ByteBufCodecs.DOUBLE,
            GrappleUpdatePacket::z,
            GrappleUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
