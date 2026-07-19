package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FinishLevelPacket(boolean overlay) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FinishLevelPacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("finish_level"));

    public static final StreamCodec<ByteBuf, FinishLevelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            FinishLevelPacket::overlay,
            FinishLevelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
