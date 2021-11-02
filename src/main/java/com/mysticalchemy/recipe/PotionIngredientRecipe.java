package com.mysticalchemy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.init.RecipeInit;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionIngredientRecipe extends SpecialRecipe {

	private ArrayList<Item> matchItems;
	private HashMap<Effect, Float> effects;
	private boolean makesSplash = false;
	private boolean makesLingering = false;
	private int durationAdded = 0;
	
	private ResourceLocation tagResource = null;
	
	public PotionIngredientRecipe(ResourceLocation idIn) {
		super(idIn);
		matchItems = new ArrayList<Item>();
		effects = new HashMap<Effect, Float>();
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		if (inv.getContainerSize() == 1) {
			return getMatchItems().contains(inv.getItem(0).getItem());
		}
		return false;
	}

	@Override
	public ItemStack assemble(CraftingInventory inv) {
		return new ItemStack(Items.POTION);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width == 1 && height == 1;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RecipeInit.POTION_INGREDIENT_SERIALIZER.get();
	}

	@Override
	public IRecipeType<?> getType() {
		return RecipeInit.POTION_RECIPE_TYPE;
	}
	
	public HashMap<Effect,Float> getEffects(){		
		return effects;
	}
	
	public ArrayList<Item> getMatchItems(){
		if (tagResource != null) {
			matchItems.addAll(ItemTags.getAllTags().getTag(tagResource).getValues());
			tagResource = null;
		}
		
		return matchItems;
	}
	
	public boolean getMakesSplash() {
		return makesSplash;
	}
	
	public boolean getMakesLingering() {
		return makesLingering;
	}
	
	public int getDurationAdded() {
		return durationAdded;
	}
	
	public static class Serializer extends SpecialRecipeSerializer<PotionIngredientRecipe>
	{
		public Serializer(Function<ResourceLocation, PotionIngredientRecipe> patternMap) {
			super(patternMap);
		}		
		
		@Override
		public PotionIngredientRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			PotionIngredientRecipe recipe = new PotionIngredientRecipe(recipeId);
			
			if (json.has("item")) {
				recipe.matchItems.add( ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString())) );
			}else if (json.has("tag")) {
				recipe.tagResource = new ResourceLocation(json.get("tag").getAsString());
			}else {
				MysticAlchemy.LOGGER.error("Potion Ingredient Recipe is missing item or tag field");
			}
			
			if (json.has("effects")) {
				JsonElement effectsElem = json.get("effects");
				if (effectsElem.isJsonArray()) {
					JsonArray elements = effectsElem.getAsJsonArray();
					elements.forEach(e -> {
						if (e.isJsonObject()) {
							JsonObject eObj = e.getAsJsonObject();
							if (eObj.has("effect") && eObj.has("strength")) {
								Effect eff = ForgeRegistries.POTIONS.getValue(new ResourceLocation(eObj.get("effect").getAsString()));
								if (eff != null)
									recipe.effects.put(eff, eObj.get("strength").getAsFloat());
								else
									MysticAlchemy.LOGGER.warn("Failed to resolve potion effect " + eObj.get("effect").getAsString() + " in " + recipeId.toString());
							}else {
								MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects element is missing effect or strength property");
							}
						}else {
							MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects element is not a json object");
						}
					});
				}else {
					MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects field is not an array");
				}
			}else {
				MysticAlchemy.LOGGER.error("Potion Ingredient Recipe is missing effects field");
			}
			
			if (json.has("duration_added"))
				recipe.durationAdded = json.get("duration_added").getAsInt();
			
			if (json.has("splash_catalyst"))
				recipe.makesSplash = json.get("splash_catalyst").getAsBoolean();
			
			if (json.has("lingering_catalyst"))
				recipe.makesLingering = json.get("lingering_catalyst").getAsBoolean();
			
			return recipe;
		}
		
		@Override
		public PotionIngredientRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
			PotionIngredientRecipe recipe = new PotionIngredientRecipe(recipeId);
			
			recipe.makesLingering = buffer.readBoolean();
			recipe.makesSplash = buffer.readBoolean();
			recipe.durationAdded = buffer.readInt();
			
			boolean hasTagResource = buffer.readBoolean();
			if (hasTagResource)
				recipe.tagResource = buffer.readResourceLocation();			
			
			int numItems = buffer.readInt();
			for (int i = 0; i < numItems; ++i)
				recipe.matchItems.add(ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation()));
			
			int numEffects = buffer.readInt();
			for (int i = 0; i < numEffects; ++i)
				recipe.effects.put(ForgeRegistries.POTIONS.getValue(buffer.readResourceLocation()), buffer.readFloat());
			
			return recipe;
		}
		
		@Override
		public void toNetwork(PacketBuffer buffer, PotionIngredientRecipe recipe) {
			buffer.writeBoolean(recipe.makesLingering);
			buffer.writeBoolean(recipe.makesSplash);
			buffer.writeInt(recipe.durationAdded);
			
			buffer.writeBoolean(recipe.tagResource != null);
			if (recipe.tagResource != null)
				buffer.writeResourceLocation(recipe.tagResource);
			
			buffer.writeInt(recipe.matchItems.size());
			recipe.matchItems.forEach(i -> buffer.writeResourceLocation(i.getRegistryName()));
			
			buffer.writeInt(recipe.effects.size());
			recipe.effects.forEach((e,f) -> {
				buffer.writeResourceLocation(e.getRegistryName());
				buffer.writeFloat(f);
			});
		}

	}
}
