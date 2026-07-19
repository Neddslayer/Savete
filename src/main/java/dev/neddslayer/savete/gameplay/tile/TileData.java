package dev.neddslayer.savete.gameplay.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public record TileData(ResourceLocation structureLocation, Vec3i tileOffset, Vec3i tileMovement) {

    public static final Codec<TileData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(TileData::structureLocation),
            Vec3i.CODEC.fieldOf("offset").forGetter(TileData::tileOffset),
            Vec3i.CODEC.fieldOf("movement").forGetter(TileData::tileMovement)
    ).apply(instance, TileData::new));
    public static final Codec<Holder<TileData>> CODEC = RegistryFileCodec.create(TileRegistry.TILE_DATA, DIRECT_CODEC);
}
