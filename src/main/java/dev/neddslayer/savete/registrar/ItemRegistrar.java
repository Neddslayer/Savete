package dev.neddslayer.savete.registrar;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.item.GrapplingHookItem;
import dev.neddslayer.savete.item.RiftOpenerItem;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

import static dev.neddslayer.savete.Savete.MODID;

public class ItemRegistrar {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Holder<ArmorMaterial> VOID_SUIT = ARMOR_MATERIALS.register("void_suit", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 4);
        map.put(ArmorItem.Type.LEGGINGS, 7);
        map.put(ArmorItem.Type.CHESTPLATE, 9);
        map.put(ArmorItem.Type.HELMET, 4);
        map.put(ArmorItem.Type.BODY, 12);
    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(Items.ARMADILLO_SCUTE), List.of(new ArmorMaterial.Layer(Savete.path("void_suit"))), 0.0F, 0.0F));

    public static final DeferredItem<BlockItem> VOID_TORCH_BLOCKITEM = ITEMS.registerSimpleBlockItem(BlockRegistrar.VOID_TORCH);
    public static final DeferredItem<BlockItem> GEMSTONE_BLOCKITEM = ITEMS.registerSimpleBlockItem(BlockRegistrar.GEMSTONE);
    public static final DeferredItem<BlockItem> LAUNCHER_BLOCKITEM = ITEMS.registerSimpleBlockItem(BlockRegistrar.LAUNCHER);
    public static final DeferredItem<BlockItem> FINISH_LEVEL_BLOCKITEM = ITEMS.registerSimpleBlockItem(BlockRegistrar.FINISH_LEVEL);

    public static final DeferredItem<RiftOpenerItem> RIFT_OPENER_ITEM = ITEMS.registerItem("rift_opener", RiftOpenerItem::new, new Item.Properties().stacksTo(1).durability(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ArmorItem> VOID_SUIT_HELMET = ITEMS.registerItem("void_suit_helmet", p -> new ArmorItem(VOID_SUIT, ArmorItem.Type.HELMET, p), new Item.Properties().stacksTo(1).durability(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<GrapplingHookItem> GRAPPLING_HOOK = ITEMS.registerItem("grappling_hook", GrapplingHookItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("excelsior_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.voidtunnels")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> ItemStack.EMPTY).displayItems((parameters, output) -> {
        output.accept(VOID_TORCH_BLOCKITEM);
        output.accept(GEMSTONE_BLOCKITEM);
        output.accept(LAUNCHER_BLOCKITEM);
        output.accept(FINISH_LEVEL_BLOCKITEM);

        output.accept(RIFT_OPENER_ITEM);
        output.accept(VOID_SUIT_HELMET);
    }).build());

    public static void bootstrap(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
