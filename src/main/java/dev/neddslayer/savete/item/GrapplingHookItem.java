package dev.neddslayer.savete.item;

import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.movement.GrappleController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GrapplingHookItem extends Item {
    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide) {
            if (entity instanceof LocalPlayer player) {
                if (player.getItemInHand(InteractionHand.MAIN_HAND).is(this)) {
                    LocalPlayerManager.setPlayerController(GrappleController.INSTANCE);
                } else {
                    LocalPlayerManager.setPlayerController(null);
                }
            }
        }
    }
}
