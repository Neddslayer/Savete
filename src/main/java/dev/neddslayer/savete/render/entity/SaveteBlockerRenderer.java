package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.SaveteBlockerEntity;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SaveteBlockerRenderer extends LateEntityRenderer<SaveteBlockerEntity> {
    public SaveteBlockerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderLate(SaveteBlockerEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        MatrixStack stack = VeilRenderBridge.create(poseStack);
        EffectHost host = new EffectHost() {
            @Override
            public float getValue(String name) {
                return 0;
            }

            @Override
            public String getName() {
                return "savete:rift";
            }

            @Override
            public void update(float partialTick) {

            }
        };

        ShaderInstance instance = VeilRenderBridge.toShaderInstance(VeilRenderSystem.renderer().getShaderManager().getShader(Savete.path("flare/blocker")));
        instance.safeGetUniform("Players").set(p_entity.getCapturedPlayers().size() / (float)p_entity.level().players().size());

        FlareEffectManager.getTemplate(Savete.path("blocker")).render(host, stack, partialTick);
    }

    @Override
    public boolean shouldRender(SaveteBlockerEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
