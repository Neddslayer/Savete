package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import foundry.veil.api.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record UpdateGemstoneCountPacket(float newTotal, int rawCount, int totalGemstonesInLevel, Vector3f position) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateGemstoneCountPacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("update_gemstone_count"));

    public static final StreamCodec<ByteBuf, UpdateGemstoneCountPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            UpdateGemstoneCountPacket::newTotal,
            ByteBufCodecs.VAR_INT,
            UpdateGemstoneCountPacket::rawCount,
            ByteBufCodecs.VAR_INT,
            UpdateGemstoneCountPacket::totalGemstonesInLevel,
            ByteBufCodecs.VECTOR3F,
            UpdateGemstoneCountPacket::position,
            UpdateGemstoneCountPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
