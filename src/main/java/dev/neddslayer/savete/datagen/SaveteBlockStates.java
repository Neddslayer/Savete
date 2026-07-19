package dev.neddslayer.savete.datagen;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.registrar.BlockRegistrar;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static dev.neddslayer.savete.Savete.MODID;

public class SaveteBlockStates extends BlockStateProvider {
    public SaveteBlockStates(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile exampleModel = models().getExistingFile(Savete.path("void_torch"));
        simpleBlockWithItem(BlockRegistrar.VOID_TORCH.get(), exampleModel);

        simpleBlockWithItem(BlockRegistrar.GEMSTONE.get(), cubeAll(BlockRegistrar.GEMSTONE.get()));
        simpleBlockWithItem(BlockRegistrar.LAUNCHER.get(), cubeAll(BlockRegistrar.LAUNCHER.get()));
    }
}
