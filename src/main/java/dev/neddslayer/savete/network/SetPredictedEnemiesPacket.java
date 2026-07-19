package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public record SetPredictedEnemiesPacket(List<EntityType<?>> entities) implements CustomPacketPayload {
    public static final Type<SetPredictedEnemiesPacket> TYPE = new Type<>(Savete.path("set_predicted_enemies"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPredictedEnemiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE).apply(ByteBufCodecs.list()),
            SetPredictedEnemiesPacket::entities,
            SetPredictedEnemiesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
