package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid=MysticAlchemy.MODID, bus=Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MysticAlchemy.MODID);
	public static final RegistryObject<RecipeSerializer<PotionIngredientRecipe>> POTION_INGREDIENT_SERIALIZER = SERIALIZERS.register("potion_ingredient", PotionIngredientRecipe.Serializer::new);

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MysticAlchemy.MODID);
	public static RegistryObject<RecipeType<PotionIngredientRecipe>> POTION_RECIPE_TYPE = RECIPE_TYPES.register("potion-ingredient-type", () -> new RecipeType<PotionIngredientRecipe>(){
		ResourceLocation potion_type = new ResourceLocation(MysticAlchemy.MODID, "potion-ingredient-type");

		@Override
		public String toString() {
			return potion_type.toString();
		}
	});
}
