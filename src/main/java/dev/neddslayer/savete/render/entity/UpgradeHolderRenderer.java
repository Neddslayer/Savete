package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.UpgradeHolderEntity;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.List;

public class UpgradeHolderRenderer extends EntityRenderer<UpgradeHolderEntity> {
    public static final ResourceLocation UPGRADE_LOCATION = Savete.path("textures/atlas/upgrades.png");

    public UpgradeHolderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UpgradeHolderEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(-0.5, 0, -0.5);

        BakedModel upgradeModel = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(p_entity.getUpgrade().getTextureLocation().withPath("upgrade/" + p_entity.getUpgrade().getTextureLocation().getPath()), "standalone"));
        List<BakedModel> renderPasses = upgradeModel.getRenderPasses(ItemStack.EMPTY, true);
        for (int i = 0; i < renderPasses.size(); i++) {
            VertexConsumer vertexconsumer;
            if (i == 0) vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(UPGRADE_LOCATION));
            else vertexconsumer = bufferSource.getBuffer(VeilRenderType.get(Savete.path("upgrade_glow"), UPGRADE_LOCATION));
            Minecraft.getInstance().getItemRenderer().renderModelLists(renderPasses.get(i), ItemStack.EMPTY, packedLight, OverlayTexture.NO_OVERLAY, poseStack, vertexconsumer);
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(UpgradeHolderEntity upgradeHolderEntity) {
        return UPGRADE_LOCATION;
    }
}
