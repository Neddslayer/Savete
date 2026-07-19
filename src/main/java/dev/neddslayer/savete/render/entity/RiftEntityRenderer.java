package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.RiftEntity;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class RiftEntityRenderer extends EntityRenderer<RiftEntity> {
    private static ResourceLocation FLARE_EFFECT = Savete.path("rift");

    public RiftEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RiftEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        stack.rotate(new Quaterniond().rotateTo(new Vector3d(0, -1, 0), p_entity.getNormal()).rotateLocalX(Mth.HALF_PI));

        ShaderProgram program = VeilRenderSystem.renderer().getShaderManager().getShader(Savete.path("flare/rift"));
        if (program != null) {
            ShaderInstance shader = VeilRenderBridge.toShaderInstance(program);
            shader.safeGetUniform("EntityPos").set((float) p_entity.getPosition(partialTick).x, (float) p_entity.getPosition(partialTick).y, (float) p_entity.getPosition(partialTick).z);
        }
        FlareEffectManager.getTemplate(FLARE_EFFECT).render(host, stack, partialTick);

        stack.matrixPop();
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEntity riftEntity) {
        return null;
    }

    @Override
    public boolean shouldRender(RiftEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }
}
