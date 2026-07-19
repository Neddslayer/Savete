package dev.neddslayer.savete.network;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AddClientUpgrade(UpgradeType<? extends AbstractUpgrade> upgradeType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AddClientUpgrade> TYPE = new CustomPacketPayload.Type<>(Savete.path("add_client_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddClientUpgrade> STREAM_CODEC = StreamCodec.composite(
            UpgradeType.STREAM_CODEC,
            AddClientUpgrade::upgradeType,
            AddClientUpgrade::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
