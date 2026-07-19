package dev.neddslayer.savete.entity;

import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeRegistry;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import dev.neddslayer.savete.render.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static dev.neddslayer.savete.render.GuiHelper.upgradeLocationToTranslatable;

public class UpgradeHolderEntity extends TemporaryGameplayEntity implements IVoidTooltipHolder {
    public static final EntityDataAccessor<UpgradeType<?>> UPGRADE_LOCATION;

    public UpgradeHolderEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public UpgradeHolderEntity(Level level, UpgradeType<? extends AbstractUpgrade> upgrade) {
        this(EntityRegistrar.UPGRADE_HOLDER.get(), level);
        this.entityData.set(UPGRADE_LOCATION, upgrade);
    }

    @Override
    public void renderTooltip(GuiGraphics graphics, int x, int y) {
        Font font = Minecraft.getInstance().font;
        float cost = LocalPlayerManager.calculateCost(this.entityData.get(UPGRADE_LOCATION));
        int color = 0xFF_FFFFFF;
        if (cost > LocalPlayerManager.gemstoneCount) color = 0xFF_FF4444;

        graphics.pose().pushPose();

        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(1.5f, 1.5f, 1.5f);

        GuiHelper.drawWobblyText(graphics, font, Component.translatable(upgradeLocationToTranslatable(this.entityData.get(UPGRADE_LOCATION).getTextureLocation()) + ".name"), 0, -25, color, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), Minecraft.getInstance().levelRenderer.getTicks(), 4, 0.05, 0.25);

        graphics.pose().popPose();

        graphics.drawString(font, Component.translatable(upgradeLocationToTranslatable(this.entityData.get(UPGRADE_LOCATION).getTextureLocation()) + ".description"), x, y-15, color);

        if (cost > 0) {
            graphics.drawString(font, Component.literal(String.valueOf(cost)), x, y, color);
        }
    }

    @Override
    public Vec3 tooltipPositionOffset() {
        return new Vec3(0.5, 0.5, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            GameController.INSTANCE.informUpgradeChosen(this);
        }
        return false;
    }

    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(UPGRADE_LOCATION, UpgradeRegistry.SPEED.get());
    }

    public UpgradeType<? extends AbstractUpgrade> getUpgrade() {
        return this.entityData.get(UPGRADE_LOCATION);
    }

    static {
        UPGRADE_LOCATION = SynchedEntityData.defineId(UpgradeHolderEntity.class, EntityDataSerializer.forValueType(UpgradeType.STREAM_CODEC));
    }
}
