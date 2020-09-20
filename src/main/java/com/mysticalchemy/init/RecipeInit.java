package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.recipe.PotionIngredientRecipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=MysticAlchemy.MODID, bus=Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MysticAlchemy.MODID);
	public static final RegistryObject<SpecialRecipeSerializer<PotionIngredientRecipe>> POTION_INGREDIENT_SERIALIZER = SERIALIZERS.register("potion_ingredient", () -> new PotionIngredientRecipe.Serializer(PotionIngredientRecipe::new));

	public static IRecipeType<PotionIngredientRecipe> POTION_RECIPE_TYPE;
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event) {
		DeferredWorkQueue.runLater(
			() -> {
				initRecipeTypes();
				MysticAlchemy.LOGGER.info("MysticAlchemy -> Recipe Types Registered");
			}
		);
	}
	
	private static void initRecipeTypes() {
		ResourceLocation potion_type = new ResourceLocation(MysticAlchemy.MODID, "potion-ingredient-type");
		POTION_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, potion_type, new IRecipeType<PotionIngredientRecipe>() {
			@Override
			public String toString() {
				return potion_type.toString();
			}
		});
	}
}
