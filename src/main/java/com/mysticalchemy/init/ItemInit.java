package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.ItemCrucibleSpoon;
import com.mysticalchemy.crucible.ItemSamplingKit;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MysticAlchemy.MODID);
	
	public static RegistryObject<ItemCrucibleSpoon> SPOON = ITEMS.register("crucible_spoon", () -> new ItemCrucibleSpoon());
	public static RegistryObject<ItemSamplingKit> SIMPLE_SAMPLING_KIT = ITEMS.register("simple_sampling_kit", () -> new ItemSamplingKit(1.0f));
	public static RegistryObject<ItemSamplingKit> ADVANCED_SAMPLING_KIT = ITEMS.register("advanced_sampling_kit", () -> new ItemSamplingKit(0.1f));

	@SubscribeEvent
	public static void onCreativeMenuInit(CreativeModeTabEvent.BuildContents event) {
		if (event.getTab() == CreativeModeTabs.INGREDIENTS) {
			event.accept(SPOON.get());
			event.accept(SIMPLE_SAMPLING_KIT.get());
			event.accept(ADVANCED_SAMPLING_KIT.get());
		}
	}
}
