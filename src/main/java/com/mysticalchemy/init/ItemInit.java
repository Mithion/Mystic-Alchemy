package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.ItemCrucibleSpoon;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MysticAlchemy.MODID);
	
	public static RegistryObject<ItemCrucibleSpoon> SPOON = ITEMS.register("crucible_spoon", () -> new ItemCrucibleSpoon());
}
