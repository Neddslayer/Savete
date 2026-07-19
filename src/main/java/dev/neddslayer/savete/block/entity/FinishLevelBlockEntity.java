package dev.neddslayer.savete.block.entity;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.SaveteClient;
import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.neddslayer.savete.gameplay.GameController.isVoidTunnels;

public class FinishLevelBlockEntity extends BlockEntity {
    private final List<RiftCrack> cracks = new ArrayList<>();
    private int age;
    private final RandomSource source;
    private ParticleEmitter emitter;

    public FinishLevelBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistrar.FINISH_LEVEL.get(), pos, blockState);
        source = RandomSource.create((long) (Math.random() * Long.MAX_VALUE));
        int cracksAmount = source.nextIntBetweenInclusive(6, 12);
        for (int i = 0; i < cracksAmount; i++) {
            RiftCrack crack = new RiftCrack();
            crack.crack(0, null, source);
            cracks.add(crack);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide && isVoidTunnels(level)) {
            List<Player> players = level.getEntitiesOfClass(Player.class, AABB.ofSize(pos.getCenter(), 1.5f, 1.5f, 1.5f));
            for (Player player : players) {
                GameController.INSTANCE.informPlayerReachedEnd((ServerLevel) level, (ServerPlayer) player);
            }
        } else {
            age++;
            if (age >= 20) {
                cracks.clear();
                int cracksAmount = source.nextIntBetweenInclusive(6, 12);
                for (int i = 0; i < cracksAmount; i++) {
                    RiftCrack crack = new RiftCrack();
                    crack.crack(0, null, source);
                    cracks.add(crack);
                }
                age = 0;
            }
            if (emitter == null || emitter.isRemoved()) {
                emitter = VeilRenderSystem.renderer().getParticleManager().createEmitter(Savete.path("burst"));
                if (this.emitter != null) {
                    emitter.setPosition(pos.getCenter());
                    VeilRenderSystem.renderer().getParticleManager().addParticleSystem(emitter);
                }
            }

            if (Minecraft.getInstance().player != null)
                SaveteClient.distanceToFinish = (float) Math.min(Minecraft.getInstance().player.getEyePosition(1).distanceTo(pos.getCenter()), SaveteClient.distanceToFinish);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (emitter != null) {
            emitter.remove();
            emitter = null;
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (emitter != null) {
            emitter.remove();
            emitter = null;
        }
    }

    public int getAge() {
        return this.age;
    }

    public List<RiftCrack> getCracks() {
        return cracks;
    }

    public static class RiftCrack {
        private final List<RiftCrack> subCracks = new ArrayList<>();
        private Vec3 offset;
        public boolean hasSubCracks = false;
        public float spreadRandomDelay = 1;

        public void crack(int subLevel, @Nullable RiftCrack previousCrack, RandomSource random) {
            int subCrackAmount = Mth.clamp(random.nextInt(Mth.clamp(4 - subLevel, 0, 4)), Mth.clamp(1 - subLevel, 0, 1), 4);
            if (previousCrack == null) {
                offset = new Vec3(
                        (2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1)),
                        (2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1)),
                        (2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1))
                );
            } else {
                offset = new Vec3(
                        ensureSign((2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1)), Math.signum(previousCrack.getOffset().x)),
                        ensureSign((2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1)), Math.signum(previousCrack.getOffset().y)),
                        ensureSign((2 * random.nextDouble() * (2.0f / (subLevel + 1))) - (2.0f / (subLevel + 1)), Math.signum(previousCrack.getOffset().z))
                );
            }
            spreadRandomDelay = 0.2f + random.nextFloat() * 0.5f;

            if (subCrackAmount <= 0) return;
            hasSubCracks = true;
            for (int i = 0; i < subCrackAmount; i++) {
                RiftCrack subCrack = new RiftCrack();
                subCrack.crack(subLevel + 1, this, random);
                subCracks.add(subCrack);
            }
        }

        public Vec3 getOffset() {
            return this.offset;
        }

        public List<RiftCrack> getSubCracks() {
            return this.subCracks;
        }

        public static double ensureSign(double number, double sign) {
            return Math.abs(number) * sign;
        }
    }
}
