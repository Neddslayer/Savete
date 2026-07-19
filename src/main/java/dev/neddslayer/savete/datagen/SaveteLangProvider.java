package dev.neddslayer.savete.datagen;

import dev.neddslayer.savete.registrar.EntityRegistrar;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Supplier;

public class SaveteLangProvider extends LanguageProvider {
    public SaveteLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        addUpgrade("speed", "Hermes' Sandals", "Run 10% faster.");
        addUpgrade("super_speed", "Rocket Boots", "Run 50% faster.");
        addUpgrade("jump", "Springloaded Shoes", "Jump 25% higher.");
        addUpgrade("lowgrav", "Moon Rocks", "Lower your gravity.");
        addUpgrade("health_up", "Reinforced Organs", "Gain 2 additional hearts.");
        addUpgrade("longer_reach", "Bigger Pickaxe", "Increase your reach by one block.");

        addUpgrade("gem_multiplier", "Refinery", "Each gem is worth 50% more.");
        addUpgrade("gem_collector", "Remote Detonation", "Breaking gems has a chance to break nearby gems.");

        addUpgrade("gem_counter", "Metal Detector", "Displays how many gems you've collected.");
        addUpgrade("double_jump", "Icarus' Feather", "Allows you to jump in the air once.");
        addUpgrade("fast_fall", "Ball and Chain", "Sneaking allows you to fall quicker.");

        addUpgrade("reroll", "Reroll", "Obtain a new set of upgrades.");

        addEntity(EntityRegistrar.SQUELCH, "Squelch", "Jumps around, knocking back nearby players.");
        addEntity(EntityRegistrar.HELIOS, "Helios", "Tries to cut you off with a laser.");
        addEntity(EntityRegistrar.TANTRUM, "Tantrum", "Dashes at you quickly.");
        addEntity(EntityRegistrar.HALO, "Halo", "Follows you and charges a beam of light.");
    }

    private void addUpgrade(String id, String name, String description) {
        this.add("upgrade.savete." + id + ".name", name);
        this.add("upgrade.savete." + id + ".description", description);
    }

    private void addEntity(Supplier<? extends EntityType<?>> key, String name, String description) {
        this.addEntityType(key, name);
        this.add(key.get().getDescriptionId() + ".name", name);
        this.add(key.get().getDescriptionId() + ".description", description);
    }
}
