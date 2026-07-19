package dev.neddslayer.savete.item;

import dev.neddslayer.savete.entity.RiftEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public class RiftOpenerItem extends Item {
    public RiftOpenerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            Direction direction = context.getClickedFace();
            Vec3 normal = new Vec3(direction.getNormal().getX(), direction.getNormal().getY(), direction.getNormal().getZ()).multiply(0.501, 0.501, 0.501);
            Vec3 creationPos = context.getClickedPos().getCenter().add(normal);

            RiftEntity entity = new RiftEntity(context.getLevel(), direction);
            entity.setPos(creationPos);
            context.getLevel().addFreshEntity(entity);

            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), EquipmentSlot.MAINHAND);
        }
        return InteractionResult.SUCCESS;
    }
}
