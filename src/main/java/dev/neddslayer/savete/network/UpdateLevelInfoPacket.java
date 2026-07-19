package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateLevelInfoPacket(int level, boolean intermission) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateLevelInfoPacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("update_level_info"));

    public static final StreamCodec<ByteBuf, UpdateLevelInfoPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateLevelInfoPacket::level,
            ByteBufCodecs.BOOL,
            UpdateLevelInfoPacket::intermission,
            UpdateLevelInfoPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
