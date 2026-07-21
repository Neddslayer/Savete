package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;

public record ForceServerPlayerDeltaMovement(Vector3f delta) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ForceServerPlayerDeltaMovement> TYPE = new CustomPacketPayload.Type<>(Savete.path("force_server_player_delta_movement"));

    public static final StreamCodec<ByteBuf, ForceServerPlayerDeltaMovement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            ForceServerPlayerDeltaMovement::delta,
            ForceServerPlayerDeltaMovement::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
