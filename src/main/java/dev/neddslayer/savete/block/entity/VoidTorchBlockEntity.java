package dev.neddslayer.savete.block.entity;

import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VoidTorchBlockEntity extends BlockEntity {
    private LightRenderHandle<PointLightData> light;

    public VoidTorchBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistrar.VOID_TORCH.get(), pos, blockState);

    }

    @Override
    public void setRemoved() {
        if (this.light != null && this.level.isClientSide) {
            light.free();
            // Mark all lights dirty, otherwise strange rendering bug where light positions get mixed up occurs.
            VeilRenderSystem.renderer().getLightRenderer().getRenderers().forEach((t, r) -> r.getLights().forEach(LightRenderHandle::markDirty));
        }
        super.setRemoved();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.light == null && this.level.isClientSide) {
            PointLightData data = new PointLightData();
            data.setPosition(getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5);
            data.setRadius(20);
            data.setBrightness(1.5f);
            data.setColor(0.9f, 0.5f, 1.0f);
            light = VeilRenderSystem.renderer().getLightRenderer().addLight(data);
        }
    }
}
