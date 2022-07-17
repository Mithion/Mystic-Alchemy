package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.recipe.PotionIngredientRecipe;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid=MysticAlchemy.MODID, bus=Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MysticAlchemy.MODID);
	public static final RegistryObject<RecipeSerializer<PotionIngredientRecipe>> POTION_INGREDIENT_SERIALIZER = SERIALIZERS.register("potion_ingredient", () -> new PotionIngredientRecipe.Serializer(PotionIngredientRecipe::new));

	public static RecipeType<PotionIngredientRecipe> POTION_RECIPE_TYPE;
	
	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(
			() -> {
				initRecipeTypes();
				MysticAlchemy.LOGGER.info("MysticAlchemy -> Recipe Types Registered");
			}
		);
	}
	
	private static void initRecipeTypes() {
		ResourceLocation potion_type = new ResourceLocation(MysticAlchemy.MODID, "potion-ingredient-type");
		POTION_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, potion_type, new RecipeType<PotionIngredientRecipe>() {
			@Override
			public String toString() {
				return potion_type.toString();
			}
		});
	}
}
