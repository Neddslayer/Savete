package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import foundry.veil.api.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record SpawnQuasarParticlePacket(ResourceLocation location, Vector3f position) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpawnQuasarParticlePacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("spawn_quasar_particle"));

    public static final StreamCodec<ByteBuf, SpawnQuasarParticlePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            SpawnQuasarParticlePacket::location,
            ByteBufCodecs.VECTOR3F,
            SpawnQuasarParticlePacket::position,
            SpawnQuasarParticlePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
