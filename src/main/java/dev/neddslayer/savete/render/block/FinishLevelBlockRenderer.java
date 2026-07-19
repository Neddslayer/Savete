package dev.neddslayer.savete.render.block;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.block.entity.FinishLevelBlockEntity;
import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static java.lang.Math.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class FinishLevelBlockRenderer extends LateBlockEntityRenderer<FinishLevelBlockEntity> {
    private static final List<Vertex> sphereList = generateSphere(16, 32, 1f);
    private static final List<Integer> sphereIndicesList = generateSphereIndices(16, 32);

    private static final RenderStateShard.ShaderStateShard FINISH_LEVEL_SHADER = VeilRenderBridge.shaderState(Savete.path("finish_level"));


    private static final float SPREAD_TIME = 2.0f;

    public FinishLevelBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    private void renderSubCracks(FinishLevelBlockEntity.RiftCrack parentCrack, Vec3 crackStart, VertexConsumer consumer, PoseStack stack, float tickTime, int subLevel) {
        Vec3 parentOffset = parentCrack.getOffset();

        for (FinishLevelBlockEntity.RiftCrack crack : parentCrack.getSubCracks()) {
            float spread = Mth.clamp((tickTime / SPREAD_TIME) - (subLevel * crack.spreadRandomDelay), 0, 1.0f);

            Vec3 start = crackStart.add(parentOffset);
            Vec3 end = crack.getOffset().multiply(spread, spread, spread).add(crackStart).add(parentOffset);
            Vec3 normal = end.subtract(start).normalize();
            Vec3 vOff = new Vec3(0, 1, 0).cross(normal).normalize().multiply(0.075, 0.075, 0.075);

            consumer.addVertex(stack.last(), start.add(vOff).toVector3f()).setUv(0f, 0f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), start.add(vOff.multiply(-1, -1, -1)).toVector3f()).setUv(1f, 0f).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), end.add(vOff.multiply(-1, -1, -1)).toVector3f()).setUv(1f, 1f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), end.add(vOff).toVector3f()) .setUv(0f, 1f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);

            if (crack.hasSubCracks) {
                for (FinishLevelBlockEntity.RiftCrack subCrack : crack.getSubCracks()) {
                    renderSubCracks(subCrack, end.subtract(subCrack.getOffset().multiply(subLevel, subLevel, subLevel)), consumer, stack, tickTime, subLevel + 1);
                }
            }
        }
    }


    @Override
    public void renderLate(BlockEntity be, float v, PoseStack stack, MultiBufferSource multiBufferSource, int light, int overlay) {
        if (!(be instanceof FinishLevelBlockEntity fl)) return;
        stack.pushPose();

        stack.scale(0.5f, 0.5f, 0.5f);

        VertexConsumer consumer2 = multiBufferSource.getBuffer(VeilRenderType.get(Savete.path("finish_level")));

        for (int i : sphereIndicesList) {
            consumer2.addVertex(stack.last(), sphereList.get(i).position).setColor(1f, 1f, 1f, 1f).setUv(0f, 0f).setNormal(stack.last(), sphereList.get(i).normal.x, sphereList.get(i).normal.y, sphereList.get(i).normal.z).setLight(LightTexture.FULL_BRIGHT);
        }

        stack.popPose();

        VertexConsumer consumer = multiBufferSource.getBuffer(VeilRenderType.get(Savete.path("glow_quad")));
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        List<FinishLevelBlockEntity.RiftCrack> cracks = fl.getCracks();

        float tickTime = fl.getAge() + v;

        for (FinishLevelBlockEntity.RiftCrack crack : cracks) {
            float spread = Mth.clamp((tickTime / SPREAD_TIME), 0, 1.0f);
            Vec3 offset = crack.getOffset().multiply(spread, spread, spread);

            Vec3 start = Vec3.ZERO;
            Vec3 end = offset;
            Vec3 normal = end.subtract(start).normalize();
            Vec3 vOff = new Vec3(0, 1, 0).cross(normal).normalize().multiply(0.075, 0.075, 0.075);

            consumer.addVertex(stack.last(), start.add(vOff).toVector3f()).setUv(0f, 0f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), start.add(vOff.multiply(-1, -1, -1)).toVector3f()).setUv(1f, 0f).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), end.add(vOff.multiply(-1, -1, -1)).toVector3f()).setUv(1f, 1f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);
            consumer.addVertex(stack.last(), end.add(vOff).toVector3f()) .setUv(0f, 1f).setLight(LightTexture.FULL_BRIGHT)  .setColor(1f, 1f, 1f, (1.0f - tickTime / 20f)).setNormal(stack.last(), (float) normal.x, (float) normal.y, (float) normal.z);

            renderSubCracks(crack, Vec3.ZERO, consumer, stack, tickTime, 1);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(FinishLevelBlockEntity blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(FinishLevelBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    private static List<Vertex> generateSphere(int stacks, int slices, float radius) {
        List<Vertex> vertices = new ArrayList<>();

        float x, y, z, xy;                              // vertex position
        float nx, ny, nz, lengthInv = 1.0f / radius;    // vertex normal

        float sectorStep = (float) (2 * PI
                / slices);
        float stackStep = (float) (PI / stacks);
        float sectorAngle, stackAngle;

        for(int i = 0; i <= stacks; ++i)
        {
            stackAngle = (float) (PI / 2 - i * stackStep);        // starting from pi/2 to -pi/2
            xy = (float) (radius * cos(stackAngle));             // r * cos(u)
            z = (float) (radius * sin(stackAngle));              // r * sin(u)

            // add (sectorCount+1) vertices per stack
            // first and last vertices have same position and normal, but different tex coords
            for(int j = 0; j <= slices; ++j)
            {
                sectorAngle = j * sectorStep;           // starting from 0 to 2pi

                // vertex position (x, y, z)
                x = (float) (xy * cos(sectorAngle));             // r * cos(u) * cos(v)
                y = (float) (xy * sin(sectorAngle));             // r * cos(u) * sin(v)

                // normalized vertex normal (nx, ny, nz)
                nx = x * lengthInv;
                ny = y * lengthInv;
                nz = z * lengthInv;

                vertices.add(new Vertex(new Vector3f(x, y, z), new Vector3f(nx, ny, nz)));
            }
        }

        return vertices;
    }

    private static List<Integer> generateSphereIndices(int stacks, int slices) {
        List<Integer> indices = new ArrayList<>();

        int k1, k2;
        for(int i = 0; i < stacks; ++i)
        {
            k1 = i * (slices + 1);     // beginning of current stack
            k2 = k1 + slices + 1;      // beginning of next stack

            for(int j = 0; j < slices; ++j, ++k1, ++k2)
            {
                // 2 triangles per sector excluding first and last stacks
                // k1 => k2 => k1+1
                if(i != 0)
                {
                    indices.add(k1);
                    indices.add(k2);
                    indices.add(k1 + 1);
                }

                // k1+1 => k2 => k2+1
                if(i != (stacks-1))
                {
                    indices.add(k1 + 1);
                    indices.add(k2);
                    indices.add(k2 + 1);
                }
            }
        }

        return indices;
    }

    private static class Vertex {
        public Vector3f position;
        public Vector3f normal;
        public Vertex(Vector3f position, Vector3f normal) {
            this.position = position;
            this.normal = normal;
        }
    }
}
