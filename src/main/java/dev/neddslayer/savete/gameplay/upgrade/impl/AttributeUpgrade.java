package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class AttributeUpgrade extends AbstractUpgrade {
    private final Holder<Attribute> attribute;
    private final float amount;
    private final AttributeModifier.Operation operation;
    private final ResourceLocation identifier;

    public AttributeUpgrade(Holder<Attribute> attribute, float amount, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.identifier = Savete.path("attribute_upgrade_" + this.hashCode());
    }

    @Override
    public void applyToPlayer(Player player) {
        player.getAttribute(this.attribute).addTransientModifier(new AttributeModifier(this.identifier, this.amount, this.operation));
    }

    @Override
    public void removeFromPlayer(Player player) {
        System.out.println(player.getAttribute(this.attribute).removeModifier(this.identifier));
    }
}
