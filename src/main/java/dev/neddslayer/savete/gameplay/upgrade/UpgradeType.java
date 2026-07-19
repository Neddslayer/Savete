package dev.neddslayer.savete.gameplay.upgrade;

import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class UpgradeType<T extends AbstractUpgrade> {

    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeType<?>> STREAM_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, List<UpgradeType<?>>> LIST_STREAM_CODEC;

    private final Supplier<T> supplier;
    private final ResourceLocation location;

    private final int maxStack;
    private final int minLevel;
    private final int gemCost;
    private final float stackMultiplier;
    private final boolean appearsInPool;
    private final Object2IntMap<Holder<UpgradeType<?>>> requiredUpgrades;

    private UpgradeType(Supplier<T> upgradeSupplier, ResourceLocation location, int maxStack, int minLevel, int gemCost, float stackMultiplier, boolean appearsInPool, Object2IntMap<Holder<UpgradeType<?>>> requiredUpgrades) {
        this.supplier = upgradeSupplier;
        this.location = location;
        this.maxStack = maxStack;
        this.minLevel = minLevel;
        this.gemCost = gemCost;
        this.stackMultiplier = stackMultiplier;
        this.appearsInPool = appearsInPool;
        this.requiredUpgrades = requiredUpgrades;
    }

    public T getUpgrade() {
        return this.supplier.get();
    }

    public ResourceLocation getTextureLocation() {
        return this.location;
    }

    public int getGemCost() {
        return this.gemCost;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxStack() {
        return this.maxStack;
    }

    public float getStackMultiplier() {
        return this.stackMultiplier;
    }

    public boolean appearsInPool() {
        return appearsInPool;
    }

    public Object2IntMap<Holder<UpgradeType<?>>> getRequiredUpgrades() {
        return this.requiredUpgrades;
    }

    static {
        STREAM_CODEC = new StreamCodec<>() {
            private static final StreamCodec<RegistryFriendlyByteBuf, UpgradeType<? extends AbstractUpgrade>> UPGRADE_HOLDER_CODEC;

            @Override
            public UpgradeType<? extends AbstractUpgrade> decode(RegistryFriendlyByteBuf buf) {
                return UPGRADE_HOLDER_CODEC.decode(buf);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf o, UpgradeType<? extends AbstractUpgrade> upgradeType) {
                UPGRADE_HOLDER_CODEC.encode(o, upgradeType);
            }

            static {
                UPGRADE_HOLDER_CODEC = ByteBufCodecs.registry(UpgradeRegistry.UPGRADE_TYPE);
            }
        };

        LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
    }

    public static class Builder<T extends AbstractUpgrade> {
        private final Supplier<T> upgrade;
        private ResourceLocation location = ResourceLocation.withDefaultNamespace("missing");
        private int maxStack = 1;
        private int minLevel = 0;
        private int gemCost = 0;
        private float stackMultiplier = 1f;
        private boolean appearsInPool = true;
        private final Object2IntMap<Holder<UpgradeType<?>>> requiredUpgrades = new Object2IntArrayMap<>();

        private Builder(Supplier<T> supplier) {
            this.upgrade = supplier;
        }

        public static <I extends AbstractUpgrade> Builder<I> of(Supplier<I> upgrade) {
            return new Builder<>(upgrade);
        }

        public Builder<T> location(ResourceLocation location) {
            this.location = location;
            return this;
        }

        public Builder<T> maxStack(int maxStack) {
            this.maxStack = maxStack;
            return this;
        }

        public Builder<T> minLevel(int minLevel) {
            this.minLevel = minLevel;
            return this;
        }

        public Builder<T> gemCost(int gemCost) {
            this.gemCost = gemCost;
            return this;
        }

        public Builder<T> stackMultiplier(float stackMultiplier) {
            this.stackMultiplier = stackMultiplier;
            return this;
        }

        public Builder<T> setAppearsInPool(boolean appearsInPool) {
            this.appearsInPool = appearsInPool;
            return this;
        }

        public Builder<T> addRequiredUpgrade(Holder<UpgradeType<? extends AbstractUpgrade>> upgrade, int count) {
            this.requiredUpgrades.put(upgrade, count);
            return this;
        }

        public UpgradeType<T> build() {
            return new UpgradeType<>(upgrade, location, maxStack, minLevel, gemCost, stackMultiplier, appearsInPool, requiredUpgrades);
        }
    }

}
