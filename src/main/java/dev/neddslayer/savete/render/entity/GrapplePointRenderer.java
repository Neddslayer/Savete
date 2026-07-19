package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.GrapplePointEntity;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.joml.Quaterniond;

public class GrapplePointRenderer extends LateEntityRenderer<GrapplePointEntity> {
    public GrapplePointRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderLate(GrapplePointEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        stack.matrixPush();

        stack.translate(0, 0.5, 0);
        stack.rotate(new Quaterniond().rotateLocalX(0.1 * (p_entity.age + partialTick)).rotateLocalY(0.1 * (p_entity.age + partialTick)).rotateLocalZ(0.1 * (p_entity.age + partialTick)));

        FlareEffectManager.getTemplate(Savete.path("grapple_point")).render(host, stack, partialTick);

        stack.matrixPop();
    }
}
