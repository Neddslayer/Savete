package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ResetClientUpgradesPacket() implements CustomPacketPayload {
    public static final Type<ResetClientUpgradesPacket> TYPE = new Type<>(Savete.path("reset_client_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResetClientUpgradesPacket> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ResetClientUpgradesPacket>() {
        @Override
        public ResetClientUpgradesPacket decode(RegistryFriendlyByteBuf buf) {
            return new ResetClientUpgradesPacket();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf o, ResetClientUpgradesPacket resetClientUpgradesPacket) {

        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
