package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record SetUpgradesPacket(List<UpgradeType<?>> upgrades, boolean append) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetUpgradesPacket> TYPE = new CustomPacketPayload.Type<>(Savete.path("set_upgrades"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetUpgradesPacket> STREAM_CODEC = StreamCodec.composite(
            UpgradeType.LIST_STREAM_CODEC,
            SetUpgradesPacket::upgrades,
            ByteBufCodecs.BOOL,
            SetUpgradesPacket::append,
            SetUpgradesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
