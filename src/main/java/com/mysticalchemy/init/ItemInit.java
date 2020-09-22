package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.ItemCrucibleSpoon;
import com.mysticalchemy.crucible.ItemSamplingKit;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MysticAlchemy.MODID);
	
	public static RegistryObject<ItemCrucibleSpoon> SPOON = ITEMS.register("crucible_spoon", () -> new ItemCrucibleSpoon());
	public static RegistryObject<ItemSamplingKit> SIMPLE_SAMPLING_KIT = ITEMS.register("simple_sampling_kit", () -> new ItemSamplingKit(1.0f));
	public static RegistryObject<ItemSamplingKit> ADVANCED_SAMPLING_KIT = ITEMS.register("advanced_sampling_kit", () -> new ItemSamplingKit(0.1f));
}
