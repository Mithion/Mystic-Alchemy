package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.api.CreativeTabs;
import com.mysticalchemy.crucible.ItemCrucibleSpoon;
import com.mysticalchemy.crucible.ItemSamplingKit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;


@Mod.EventBusSubscriber(modid = MysticAlchemy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemInit {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MysticAlchemy.MODID);
	
	public static RegistryObject<ItemCrucibleSpoon> SPOON = ITEMS.register("crucible_spoon", () -> new ItemCrucibleSpoon());
	public static RegistryObject<ItemSamplingKit> SIMPLE_SAMPLING_KIT = ITEMS.register("simple_sampling_kit", () -> new ItemSamplingKit(1.0f));
	public static RegistryObject<ItemSamplingKit> ADVANCED_SAMPLING_KIT = ITEMS.register("advanced_sampling_kit", () -> new ItemSamplingKit(0.1f));

	@SubscribeEvent
	public static void FillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTab() == CreativeTabs.MYSTIC_ALCHEMY) {
			ITEMS.getEntries().stream().map(RegistryObject::get).forEach(event::accept);
		}
	}
}
