package dev.neddslayer.savete.render;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.render.entity.UpgradeHolderRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UpgradeModel implements IUnbakedGeometry<UpgradeModel> {
    private @Nullable ImmutableList<Material> textures;
    private final Int2ObjectMap<ExtraFaceData> layerData;
    private final Int2ObjectMap<ResourceLocation> renderTypeNames;

    private UpgradeModel(@Nullable ImmutableList<Material> textures, Int2ObjectMap<ExtraFaceData> layerData, Int2ObjectMap<ResourceLocation> renderTypeNames) {
        this.textures = textures;
        this.layerData = layerData;
        this.renderTypeNames = renderTypeNames;
    }

    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        if (this.textures == null) {
            ImmutableList.Builder<Material> builder = ImmutableList.builder();

            for(int i = 0; context.hasMaterial("layer" + i); ++i) {
                Material old = context.getMaterial("layer" + i);
                builder.add(new Material(UpgradeHolderRenderer.UPGRADE_LOCATION, old.texture()));
            }

            this.textures = builder.build();
        }

        TextureAtlasSprite particle = spriteGetter.apply(context.hasMaterial("particle") ? context.getMaterial("particle") : this.textures.getFirst());
        Transformation rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }

        RenderTypeGroup normalRenderTypes = new RenderTypeGroup(RenderType.translucent(), NeoForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
        CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());

        for(int i = 0; i < this.textures.size(); ++i) {
            TextureAtlasSprite sprite = spriteGetter.apply(this.textures.get(i));

            List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite, this.layerData.get(i));
            List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, ($) -> sprite, modelState);

            ResourceLocation renderTypeName = this.renderTypeNames.get(i);
            RenderTypeGroup renderTypes = renderTypeName != null ? context.getRenderType(renderTypeName) : null;

            builder.addQuads(renderTypes != null ? renderTypes : normalRenderTypes, quads);
        }

        return builder.build();
    }

    public static final class Loader implements IGeometryLoader<UpgradeModel> {
        public static final UpgradeModel.Loader INSTANCE = new UpgradeModel.Loader();

        public Loader() {
        }

        public UpgradeModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            Int2ObjectOpenHashMap<ResourceLocation> renderTypeNames = new Int2ObjectOpenHashMap<>();
            if (jsonObject.has("render_types")) {
                JsonObject renderTypes = jsonObject.getAsJsonObject("render_types");

                for(Map.Entry<String, JsonElement> entry : renderTypes.entrySet()) {
                    ResourceLocation renderType = ResourceLocation.parse(entry.getKey());

                    for(JsonElement layer : entry.getValue().getAsJsonArray()) {
                        if (renderTypeNames.put(layer.getAsInt(), renderType) != null) {
                            throw new JsonParseException("Registered duplicate render type for layer " + layer);
                        }
                    }
                }
            }

            Int2ObjectArrayMap<ExtraFaceData> emissiveLayers = new Int2ObjectArrayMap<>();
            if (jsonObject.has("forge_data")) {
                throw new JsonParseException("forge_data should be replaced by neoforge_data");
            } else {
                if (jsonObject.has("neoforge_data")) {
                    JsonObject forgeData = jsonObject.get("neoforge_data").getAsJsonObject();
                    this.readLayerData(forgeData, "layers", emissiveLayers);
                }

                return new UpgradeModel(null, emissiveLayers, renderTypeNames);
            }
        }

        private void readLayerData(JsonObject jsonObject, String name, Int2ObjectMap<ExtraFaceData> layerData) {
            if (jsonObject.has(name)) {
                JsonObject fullbrightLayers = jsonObject.getAsJsonObject(name);

                for(Map.Entry<String, JsonElement> entry : fullbrightLayers.entrySet()) {
                    int layer = Integer.parseInt((String)entry.getKey());
                    ExtraFaceData data = ExtraFaceData.read((JsonElement)entry.getValue(), ExtraFaceData.DEFAULT);
                    layerData.put(layer, data);
                }

            }
        }
    }
}
